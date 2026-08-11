package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RimefireTorqueTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent entering before the subtype is chosen does not trigger")
    void enteringBeforeSubtypeChoiceDoesNotTrigger() {
        Permanent torque = addTorque(null, 0);
        harness.setHand(player1, List.of(artifact("Unchosen Wizard Relic", CardSubtype.WIZARD)));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("A permanent you control of the chosen type adds a charge counter")
    void matchingPermanentAddsChargeCounter() {
        Permanent torque = addTorque(CardSubtype.WIZARD, 0);

        harness.setHand(player1, List.of(artifact("Wizard Relic", CardSubtype.WIZARD)));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's or differently typed permanent does not add a charge counter")
    void nonmatchingPermanentDoesNotAddChargeCounter() {
        Permanent torque = addTorque(CardSubtype.WIZARD, 0);

        harness.setHand(player1, List.of(artifact("Elf Relic", CardSubtype.ELF)));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Card opponentWizard = artifact("Opponent Wizard Relic", CardSubtype.WIZARD);
        opponentWizard.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(opponentWizard));
        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing three charge counters copies the next instant or sorcery")
    void copiesNextInstantOrSorcery() {
        Permanent torque = addTorque(CardSubtype.WIZARD, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(torque.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    private Permanent addTorque(CardSubtype chosenSubtype, int chargeCounters) {
        Permanent torque = new Permanent(new RimefireTorque());
        torque.setChosenSubtype(chosenSubtype);
        torque.setCounterCount(CounterType.CHARGE, chargeCounters);
        gd.playerBattlefields.get(player1.getId()).add(torque);
        return torque;
    }

    private Card artifact(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("{0}");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
