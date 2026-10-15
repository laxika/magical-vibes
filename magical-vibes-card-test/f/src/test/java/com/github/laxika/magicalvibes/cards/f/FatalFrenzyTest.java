package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.b.BruteForce;
import com.github.laxika.magicalvibes.cards.e.Enslave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalFrenzy.class, BloodKnight.class, FrozenAether.class, BruteForce.class, Enslave.class})
class FatalFrenzyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature you control +X/+0 where X is its power and trample")
    void boostsByTargetPowerAndGrantsTrample() {
        harness.addToBattlefield(player1, new BloodKnight());
        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent bears = findPermanent(player1, "Blood Knight");
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower * 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(after.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Uses the target's power when Fatal Frenzy resolves")
    void usesTargetPowerAtResolution() {
        harness.addToBattlefield(player1, new BloodKnight());
        harness.setHand(player1, List.of(new FatalFrenzy(), new BruteForce()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent bears = findPermanent(player1, "Blood Knight");
        harness.castInstant(player1, 0, bears.getId());
        harness.castInstant(player1, 0, bears.getId());

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(10);
    }

    @Test
    @DisplayName("Sacrifices the target at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new BloodKnight());

        Permanent bears = findPermanent(player1, "Blood Knight");
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player1, "Blood Knight");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blood Knight");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blood Knight");
        harness.assertInGraveyard(player1, "Blood Knight");
    }

    @Test
    @DisplayName("The delayed sacrifice ability has Fatal Frenzy as its source")
    void delayedAbilityKeepsSpellSource() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BloodKnight());
        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(FatalFrenzy.class);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot sacrifice the creature after an opponent gains control")
    void doesNotSacrificeCreatureUnderOpponentControl() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BloodKnight());
        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.setHand(player2, List.of(new Enslave()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Blood Knight");

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Blood Knight");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player2, new BloodKnight());

        Permanent opponentBears = findPermanent(player2, "Blood Knight");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.setHand(player1, List.of(new FatalFrenzy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addToBattlefield(player1, new FrozenAether());

        Permanent frozenAether = findPermanent(player1, "Frozen Aether");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, frozenAether.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
