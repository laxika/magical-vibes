package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeliodsEmissary.class, TravelingPhilosopher.class, LightningStrike.class})
class HeliodsEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("When Heliod's Emissary attacks, it taps a target creature an opponent controls")
    void creatureAttackTapsOpponentCreature() {
        addCreatureReady(player1, new HeliodsEmissary());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and its attack trigger still taps an opponent creature")
    void bestowBoostsAndGrantsAttackTrigger() {
        Permanent bear = addCreatureReady(player1, new TravelingPhilosopher());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger only allows creatures controlled by an opponent")
    void attackTriggerRestrictsTargets() {
        addCreatureReady(player1, new HeliodsEmissary());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    void normalCastingNeedsNoTarget() {
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heliod's Emissary");
        assertThat(findPermanent(player1, "Heliod's Emissary").isAttached()).isFalse();
    }

    @Test
    void bestowResolvesAsCreatureWhenTargetDies() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castWithAlternateCost(player1, 0, host.getId());

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertOnBattlefield(player1, "Heliod's Emissary");
        assertThat(findPermanent(player1, "Heliod's Emissary").isAttached()).isFalse();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Heliod's Emissary"))).isTrue();
    }

    @Test
    void auraControllerChoosesTargetWhenOpponentsEnchantedCreatureAttacks() {
        Permanent host = addCreatureReady(player2, new TravelingPhilosopher());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(host.getId(), victim.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void bestowedEmissaryBecomesCreatureWhenEnchantedCreatureDies() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new HeliodsEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent emissary = findPermanent(player1, "Heliod's Emissary");
        assertThat(emissary.getAttachedTo()).isEqualTo(host.getId());

        harness.setHand(player2, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Philosopher");
        harness.assertNotInGraveyard(player1, "Heliod's Emissary");
        assertThat(findPermanent(player1, "Heliod's Emissary")).isSameAs(emissary);
        assertThat(emissary.isAttached()).isFalse();
        assertThat(gqs.isCreature(gd, emissary)).isTrue();
    }

    @Test
    void attackWithoutOpposingCreaturesDoesNotRequireTargetChoice() {
        addCreatureReady(player1, new HeliodsEmissary());
        harness.addToBattlefield(player1, new TravelingPhilosopher());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
