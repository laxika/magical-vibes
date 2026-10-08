package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SylvanAwakening;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VatOfRebirth.class, AngelsFeather.class, GrizzlyBears.class, Forest.class})
class VatOfRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Adds an oil counter when an artifact or creature you control is put into a graveyard")
    void addsOilCounterForOwnArtifactOrCreature() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        putIntoGraveyard(artifact);
        putIntoGraveyard(creature);

        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ignores an opponent's creature and a land you control")
    void ignoresOpponentCreatureAndOwnLand() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        putIntoGraveyard(opponentCreature);
        putIntoGraveyard(land);

        assertThat(vat.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Triggers for a permanent you control even when it goes to its owner's graveyard")
    void triggersForStolenCreatureYouControl() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(stolenCreature);
        gd.playerBattlefields.get(player1.getId()).add(stolenCreature);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());

        putIntoGraveyard(stolenCreature);

        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes four oil counters and returns a target creature from the graveyard")
    void removesOilCountersAndReanimatesCreature() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(vat.getCounterCount(CounterType.OIL)).isZero();
        assertThat(vat.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target")
    void rejectsNoncreatureTarget() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new AngelsFeather();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @CardUsed({SylvanAwakening.class})
    @DisplayName("Counts a land that was a creature immediately before leaving the battlefield")
    void countsAnimatedLandDeath() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        enterMainWithPriority(player1);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        putIntoGraveyard(land);

        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for its own departure")
    void doesNotTriggerForItself() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, vat));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vat of Rebirth");
    }

    @Test
    @DisplayName("Requires at least four oil counters")
    void rejectsInsufficientOilCounters() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 3);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(vat.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(vat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void rejectsActivationOutsideMainPhase() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void rejectsActivationOnOpponentsTurn() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate with an oil-counter trigger on the stack")
    void rejectsActivationWithNonemptyStack() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Pays exactly four counters immediately even if the target later leaves the graveyard")
    void paysCostsBeforeResolutionAndDoesNotReturnMissingTarget() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 6);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(vat.isTapped()).isTrue();
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void rejectsTappedSource() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        vat.tap();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires black mana in addition to two generic mana")
    void rejectsManaWithoutBlack() {
        Permanent vat = harness.addToBattlefieldAndReturn(player1, new VatOfRebirth());
        vat.setCounterCount(CounterType.OIL, 4);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vat.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(vat.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
