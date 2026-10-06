package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WebShooters;
import com.github.laxika.magicalvibes.cards.h.HotDogCart;
import com.github.laxika.magicalvibes.cards.s.SpiderBot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingBrainMechanicalMarvel.class, HotDogCart.class, WebShooters.class, SpiderBot.class})
class LivingBrainMechanicalMarvelTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, animates and untaps a target non-Equipment artifact")
    void animatesAndUntapsTargetArtifact() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent cart = harness.addToBattlefieldAndReturn(player1, new HotDogCart());
        cart.tap();

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(cart.getId());

        harness.handlePermanentChosen(player1, cart.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cart)).isEqualTo(3);
        assertThat(cart.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat trigger excludes Equipment and opponents' artifacts")
    void onlyTargetsControlledNonEquipmentArtifacts() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new WebShooters());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HotDogCart());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .doesNotContain(equipment.getId(), opponentArtifact.getId());
    }

    @Test
    void animationEndsDuringCleanup() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent cart = harness.addToBattlefieldAndReturn(player1, new HotDogCart());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, cart.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, cart)).isTrue();
        assertThat(gqs.isArtifact(gd, cart)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cart)).isFalse();
        assertThat(gqs.isArtifact(gd, cart)).isTrue();
    }

    @Test
    void canTargetAndUntapItself() {
        Permanent brain = harness.addToBattlefieldAndReturn(player1, new LivingBrainMechanicalMarvel());
        brain.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, brain.getId());
        harness.passBothPriorities();

        assertThat(brain.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, brain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brain)).isEqualTo(3);
    }

    @Test
    void overwritesExistingArtifactCreaturesBasePowerAndToughnessUntilCleanup() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent bot = harness.addToBattlefieldAndReturn(player1, new SpiderBot());
        bot.tap();

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bot.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(3);
        assertThat(bot.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bot)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bot)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent brain = harness.addToBattlefieldAndReturn(player1, new LivingBrainMechanicalMarvel());
        Permanent cart = harness.addToBattlefieldAndReturn(player1, new HotDogCart());
        brain.tap();
        cart.tap();

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.isCreature(gd, cart)).isFalse();
        assertThat(cart.isTapped()).isTrue();
        assertThat(brain.isTapped()).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
