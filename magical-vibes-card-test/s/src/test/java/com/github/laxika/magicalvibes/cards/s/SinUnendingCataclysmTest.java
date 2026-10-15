package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoryosVengeance;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinUnendingCataclysm.class, GrizzlyBears.class, GoryosVengeance.class, SolRing.class, HardenedScales.class})
class SinUnendingCataclysmTest extends BaseCardTest {

    @Test
    @DisplayName("Removes all counters and enters with twice the number removed")
    void removesCountersAndEntersWithTwiceTheCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownPermanent.setCounterCount(CounterType.CHARGE, 1);
        opposingPermanent.setCounterCount(CounterType.LOYALTY, 3);

        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        harness.castFromHand(player1, sin, "{5}{G}{U}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownPermanent.getId(), opposingPermanent.getId()));

        assertThat(ownPermanent.getTotalCounterCount()).isZero();
        assertThat(opposingPermanent.getTotalCounterCount()).isZero();
        assertThat(findPermanent(player1, "Sin, Unending Cataclysm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(12);
    }

    @Test
    @DisplayName("Moves its counters to a creature and then shuffles into its owner's library")
    void movesCountersAndShufflesOnDeath() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        harness.setLibrary(player1, new ArrayList<>());
        harness.castFromHand(player1, sin, "{5}{G}{U}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent sinPermanent = findPermanent(player1, "Sin, Unending Cataclysm");
        sinPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        sinPermanent.setCounterCount(CounterType.CHARGE, 2);
        sinPermanent.setMarkedDamage(10);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sin);
        assertThat(gd.playerDecks.get(player1.getId())).contains(sin);
    }

    @Test
    void removesCountersFromNoncreatureArtifactsAndEnchantments() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HardenedScales());
        artifact.setCounterCount(CounterType.CHARGE, 2);
        enchantment.setCounterCount(CounterType.CHARGE, 3);

        harness.castFromHand(player1, new SinUnendingCataclysm(), "{5}{G}{U}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId(), enchantment.getId()));

        assertThat(artifact.getTotalCounterCount()).isZero();
        assertThat(enchantment.getTotalCounterCount()).isZero();
        assertThat(findPermanent(player1, "Sin, Unending Cataclysm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
    }

    @Test
    void entersWithoutCountersWhenThereAreNoEligiblePermanents() {
        harness.castFromHand(player1, new SinUnendingCataclysm(), "{5}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Sin, Unending Cataclysm").getTotalCounterCount()).isZero();
    }

    @Test
    void leavesUnchosenPermanentsCountersAlone() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        chosen.setCounterCount(CounterType.CHARGE, 2);
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.castFromHand(player1, new SinUnendingCataclysm(), "{5}{G}{U}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getTotalCounterCount()).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanent(player1, "Sin, Unending Cataclysm")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void deathWithoutCountersStillTargetsAndShuffles() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, sin);
        dying.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getTotalCounterCount()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sin);
        assertThat(gd.playerDecks.get(player1.getId())).contains(sin);
    }

    @Test
    void noControlledCreatureMeansSinIsNotShuffled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, sin);
        dying.setCounterCount(CounterType.CHARGE, 2);
        dying.setMarkedDamage(5);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sin);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sin);
    }

    @Test
    void illegalTargetPreventsTheShuffleToo() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        Permanent dying = harness.addToBattlefieldAndReturn(player1, sin);
        dying.setCounterCount(CounterType.CHARGE, 2);
        dying.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sin);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sin);
    }

    @Test
    void originalDeathAbilityCannotShuffleSinAfterItLeavesAndReentersTheGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        SinUnendingCataclysm sin = new SinUnendingCataclysm();
        sin.setOwnerId(player2.getId());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, sin);
        gd.stolenCreatures.put(dying.getId(), player2.getId());
        dying.setCounterCount(CounterType.CHARGE, 2);
        dying.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sin);

        harness.setHand(player2, List.of(new GoryosVengeance()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, sin.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        Permanent returned = findPermanent(player2, "Sin, Unending Cataclysm");
        returned.setMarkedDamage(5);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sin);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(sin);
    }
}
