package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.a.AquitectsWill;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({WandOfTheElements.class, Island.class, Mountain.class, MarchOfTheMachines.class,
        AshayaSoulOfTheWild.class, AquitectsWill.class})
class WandOfTheElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island creates a 2/2 blue Elemental with flying")
    void islandAbilityCreatesBlueElemental() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Sacrificing a Mountain creates a 3/3 red Elemental")
    void mountainAbilityCreatesRedElemental() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Each ability requires the matching basic land type")
    void abilitiesRequireMatchingLandType() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Each ability requires Wand of the Elements to be untapped")
    void abilitiesRequireWandToBeUntapped() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({WandOfTheElements.class, MarchOfTheMachines.class, AshayaSoulOfTheWild.class, AquitectsWill.class})
    @DisplayName("The Wand itself may be sacrificed when it is an Island")
    void canSacrificeItselfWhenItIsAnIsland() {
        Permanent wand = addCreatureReady(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());

        harness.setHand(player1, List.of(new AquitectsWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0, wand.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, wand)).contains(CardSubtype.ISLAND);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wand of the Elements");
    }

    @Test
    @DisplayName("Sacrifice and tap costs are paid before the token is created")
    void costsArePaidBeforeResolution() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(findPermanent(player1, "Wand of the Elements").isTapped()).isTrue();
        assertThat(countPermanents(player1, "Elemental")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    @DisplayName("A tapped Mountain can be sacrificed")
    void canSacrificeTappedMountain() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(findPermanent(player1, "Elemental").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Island cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsIsland() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Island");
        assertThat(findPermanent(player1, "Wand of the Elements").isTapped()).isFalse();
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }
}
