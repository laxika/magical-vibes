package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.cards.w.WatchersOfTheDead;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuthlessSniper.class, Censor.class, WatchersOfTheDead.class, SacredCat.class, Swamp.class, TormentingVoice.class})
class RuthlessSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card and paying {1} puts a -1/-1 counter on target creature")
    void cyclePayPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new RuthlessSniper());
        harness.addToBattlefield(player2, new WatchersOfTheDead());
        UUID bearsId = harness.getPermanentId(player2, "Watchers of the Dead");
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new WatchersOfTheDead())); // cycling draw
        harness.addMana(player1, ManaColor.BLUE, 1);       // cycling {U}
        harness.addMana(player1, ManaColor.COLORLESS, 1);  // the may-pay {1}

        harness.activateHandAbility(player1, 0, null); // cycle Censor -> discard trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Watchers of the Dead");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
        // May-pay {1} spent (the cycling {U} was consumed by activating the ability)
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the may-pay puts no counter and spends no mana")
    void declineDoesNotPutCounter() {
        harness.addToBattlefield(player1, new RuthlessSniper());
        harness.addToBattlefield(player2, new WatchersOfTheDead());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new WatchersOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Watchers of the Dead"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        Permanent bears = findPermanent(player2, "Watchers of the Dead");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        // The may-pay {1} is never spent when declined
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 counter can kill a 1/1 creature")
    void counterKillsOneToughnessCreature() {
        harness.addToBattlefield(player1, new RuthlessSniper());
        harness.addToBattlefield(player2, new SacredCat()); // 1/1
        UUID hawkId = harness.getPermanentId(player2, "Sacred Cat");
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new WatchersOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, hawkId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        // Sacred Cat (1/1) becomes 0/0 and dies to state-based actions
        harness.assertNotOnBattlefield(player2, "Sacred Cat");
        harness.assertInGraveyard(player2, "Sacred Cat");
    }

    @Test
    @DisplayName("Discard trigger offers creatures but not noncreature permanents as targets")
    void noncreaturePermanentIsNotALegalTarget() {
        Permanent sniper = harness.addToBattlefieldAndReturn(player1, new RuthlessSniper());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WatchersOfTheDead());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(sniper.getId(), creature.getId()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Discarding for a spell cost triggers once without cycling")
    void ordinaryDiscardPutsOneCounter() {
        harness.addToBattlefield(player1, new RuthlessSniper());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WatchersOfTheDead());
        harness.setHand(player1, List.of(new TormentingVoice(), new Censor()));
        harness.setLibrary(player1, List.of(new Censor(), new Censor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent cycling does not trigger Ruthless Sniper")
    void opponentCyclingDoesNotTrigger() {
        Permanent sniper = harness.addToBattlefieldAndReturn(player1, new RuthlessSniper());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(sniper.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Unable to pay the trigger cost leaves the target unchanged")
    void cannotPayDoesNotPutCounter() {
        Permanent sniper = harness.addToBattlefieldAndReturn(player1, new RuthlessSniper());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Censor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sniper.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(sniper.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
