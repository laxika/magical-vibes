package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagesGuile.class, ElvishWarrior.class, Island.class, Shock.class})
class MagesGuileTest extends BaseCardTest {

    @Test
    void targetCreatureGainsShroudUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void onlyTargetsCreatures() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void shroudPreventsTargetingTheCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new MagesGuile(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void cyclingDiscardsMagesGuileAndDrawsACard() {
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mage's Guile");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    void canTargetOpponentsCreatureWithoutGrantingShroudToOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudMakesAnAlreadyCastOpponentsSpellFailToResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingDiscardsImmediatelyButDrawsOnlyOnResolution() {
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Mage's Guile");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Elvish Warrior");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCannotBePaidWithOnlyColorlessMana() {
        harness.setHand(player1, List.of(new MagesGuile()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mage's Guile");
        harness.assertNotInGraveyard(player1, "Mage's Guile");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
