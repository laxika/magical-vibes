package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Facevaulter.class, BoggartShenanigans.class})
class FacevaulterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Goblin gives +2/+2")
    void sacrificeAnotherGoblinGivesBoost() {
        addCreatureReady(player1, new Facevaulter());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Facevaulter());
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Two Goblins on battlefield (Facevaulter + token) → prompted to choose the sacrifice.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Facevaulter");

        Permanent facevaulter = findPermanent(player1, "Facevaulter");
        assertThat(facevaulter.getPowerModifier()).isEqualTo(2);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Facevaulter can sacrifice itself for its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new Facevaulter());
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only Goblin on battlefield is Facevaulter → auto-sacrifice itself
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Facevaulter");
        harness.assertInGraveyard(player1, "Facevaulter");
    }

    @Test
    @DisplayName("Ability requires {B} mana to activate")
    void abilityRequiresMana() {
        addCreatureReady(player1, new Facevaulter());
        harness.addToBattlefield(player1, new Facevaulter());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new Facevaulter());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Facevaulter());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        Permanent facevaulter = findPermanent(player1, "Facevaulter");
        assertThat(facevaulter.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(facevaulter.getPowerModifier()).isEqualTo(0);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A noncreature Goblin can pay the sacrifice cost")
    void canSacrificeGoblinEnchantment() {
        Permanent facevaulter = addCreatureReady(player1, new Facevaulter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        assertThat(facevaulter.getPowerModifier()).isZero();
        harness.passBothPriorities();

        assertThat(facevaulter.getPowerModifier()).isEqualTo(2);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Facevaulter can activate and boosts accumulate")
    void tappedSummoningSickSourceCanActivateRepeatedly() {
        Permanent facevaulter = harness.addToBattlefieldAndReturn(player1, new Facevaulter());
        facevaulter.setSummoningSick(true);
        facevaulter.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Facevaulter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Facevaulter());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(facevaulter.getPowerModifier()).isZero();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(facevaulter.getPowerModifier()).isEqualTo(4);
        assertThat(facevaulter.getToughnessModifier()).isEqualTo(4);
        assertThat(facevaulter.isTapped()).isTrue();
    }
}
