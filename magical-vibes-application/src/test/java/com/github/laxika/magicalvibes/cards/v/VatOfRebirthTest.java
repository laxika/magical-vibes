package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
