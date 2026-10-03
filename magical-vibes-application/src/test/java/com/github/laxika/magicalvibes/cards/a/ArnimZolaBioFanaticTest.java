package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArnimZolaBioFanatic.class, Swamp.class})
class ArnimZolaBioFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without two creature cards in the graveyard")
    void cannotActivateWithoutTwoCreatureCardsInGraveyard() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 or more creature cards");
        assertThat(arnim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creates a tapped 2/1 Villain token with menace")
    void createsTappedVillainTokenWithMenace() {
        addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Villain");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isTrue();
        assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Noncreature cards do not satisfy the graveyard restriction")
    void noncreatureCardsDoNotCount() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new Swamp(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 or more creature cards");
        assertThat(arnim.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creature cards do not satisfy the graveyard restriction")
    void opponentsCreatureCardsDoNotCount() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic()));
        harness.setGraveyard(player2, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2 or more creature cards");
        assertThat(arnim.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard restriction is checked at activation, not resolution")
    void resolvesAfterCreatureCardsLeaveGraveyard() {
        addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Villain")).isEqualTo(1);
        assertThat(findPermanent(player1, "Villain").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays three mana and taps to create exactly one black Villain creature")
    void paysCostsAndCreatesOneBlackVillainCreature() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic(),
                new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(arnim.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(countPermanents(player1, "Villain")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Villain")).isEqualTo(1);
        assertThat(countPermanents(player2, "Villain")).isZero();
        Permanent token = findPermanent(player1, "Villain");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent arnim = harness.addToBattlefieldAndReturn(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(arnim.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves even after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent arnim = addCreatureReady(player1, new ArnimZolaBioFanatic());
        harness.setGraveyard(player1, List.of(new ArnimZolaBioFanatic(), new ArnimZolaBioFanatic()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(arnim);
        gd.playerGraveyards.get(player1.getId()).add(arnim.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Villain")).isEqualTo(1);
        assertThat(findPermanent(player1, "Villain").hasKeyword(Keyword.MENACE)).isTrue();
    }
}
