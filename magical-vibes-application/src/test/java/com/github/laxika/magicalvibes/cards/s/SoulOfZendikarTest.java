package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulOfZendikar.class})
class SoulOfZendikarTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability creates a 3/3 Beast token")
    void battlefieldAbilityCreatesBeastToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SoulOfZendikar());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Beast"))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getPower()).isEqualTo(3);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("Graveyard ability exiles the source and creates a Beast token")
    void graveyardAbilityExilesSourceAndCreatesToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new SoulOfZendikar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Soul of Zendikar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Soul of Zendikar"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Beast"))
                .hasSize(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Soul can activate twice on the opponent's turn")
    void canActivateRepeatedlyWithoutTappingOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfZendikar());
        soul.tap();
        soul.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Beast"))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getPower()).isEqualTo(3);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(3);
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(permanent.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(soul.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Insufficient green mana does not exile the graveyard source")
    void graveyardAbilityRequiresTwoGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SoulOfZendikar soul = new SoulOfZendikar();
        harness.setGraveyard(player1, List.of(soul));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Graveyard ability works on the opponent's turn and cannot reuse the exiled source")
    void graveyardAbilityWorksOnOpponentsTurnOnlyOnce() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SoulOfZendikar soul = new SoulOfZendikar();
        harness.setGraveyard(player1, List.of(soul));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.ensurePriority(player1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(soul);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Beast");
                    assertThat(permanent.getCard().getPower()).isEqualTo(3);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(3);
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(permanent.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(permanent.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
                    assertThat(permanent.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
