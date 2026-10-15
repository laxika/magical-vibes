package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DivinityOfPride;
import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.s.Scarecrone;
import com.github.laxika.magicalvibes.cards.s.SootImp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        LingeringTormentor.class,
        Eviscerate.class,
        SootImp.class,
        Scarecrone.class,
        DuskdaleWurm.class,
        DivinityOfPride.class
})
class LingeringTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns a stolen creature under its owner's control")
    void persistReturnsToOwnerInsteadOfController() {
        LingeringTormentor card = new LingeringTormentor();
        card.setOwnerId(player2.getId());
        Permanent tormentor = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, tormentor.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lingering Tormentor");
        Permanent returned = findPermanent(player2, "Lingering Tormentor");
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Lingering Tormentor");
    }

    @Test
    @DisplayName("Lethal -1/-1 counters prevent persist from returning the creature")
    void lethalMinusCountersPreventPersist() {
        Permanent tormentor = harness.addToBattlefieldAndReturn(player1, new LingeringTormentor());
        tormentor.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lingering Tormentor");
        harness.assertInGraveyard(player1, "Lingering Tormentor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the persist counter allows the returned creature to persist again")
    void persistWorksAgainAfterReturnCounterIsRemoved() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new LingeringTormentor());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0, original.getId());
        resolveAllTriggers();
        Permanent firstReturn = findPermanent(player1, "Lingering Tormentor");
        assertThat(firstReturn.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        firstReturn.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.castSorcery(player1, 0, 0, firstReturn.getId());
        resolveAllTriggers();

        Permanent secondReturn = findPermanent(player1, "Lingering Tormentor");
        assertThat(secondReturn).isNotSameAs(firstReturn);
        assertThat(secondReturn.getCard().getId()).isEqualTo(original.getCard().getId());
        assertThat(secondReturn.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Lingering Tormentor");
    }

    @Test
    @DisplayName("Persist returns Lingering Tormentor with a -1/-1 counter when it dies with none")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new LingeringTormentor());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Lingering Tormentor"));
        resolveAllTriggers();

        Permanent tormentor = findPermanent(player1, "Lingering Tormentor");
        assertThat(tormentor.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(tormentor.getEffectivePower()).isEqualTo(1);
        assertThat(tormentor.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Lingering Tormentor when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent tormentor = harness.addToBattlefieldAndReturn(player1, new LingeringTormentor());
        tormentor.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, tormentor.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Lingering Tormentor");
        harness.assertInGraveyard(player1, "Lingering Tormentor");
    }

    @Test
    @DisplayName("Fear prevents blocking by a nonblack nonartifact creature")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent attacker = addCreatureReady(player1, new LingeringTormentor());
        Permanent blocker = addCreatureReady(player2, new DuskdaleWurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fear allows blocking by a black creature")
    void fearAllowsBlackBlocker() {
        Permanent attacker = addCreatureReady(player1, new LingeringTormentor());
        Permanent blocker = addCreatureReady(player2, new SootImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows blocking by an artifact creature")
    void fearAllowsArtifactBlocker() {
        Permanent attacker = addCreatureReady(player1, new LingeringTormentor());
        Permanent blocker = addCreatureReady(player2, new Scarecrone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Persist returns Lingering Tormentor with a -1/-1 counter after it dies")
    void persistReturnsWithMinusOneMinusOneCounter() {
        Permanent attacker = addCreatureReady(player1, new LingeringTormentor());
        Permanent blocker = addCreatureReady(player2, new DivinityOfPride());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Lingering Tormentor");
        assertThat(returned).isNotSameAs(attacker);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Lingering Tormentor");
    }

    @Test
    @DisplayName("Persist does not return Lingering Tormentor if it had a -1/-1 counter")
    void persistDoesNotReturnWithMinusOneMinusOneCounter() {
        Permanent attacker = addCreatureReady(player1, new LingeringTormentor());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent blocker = addCreatureReady(player2, new DivinityOfPride());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lingering Tormentor");
        harness.assertInGraveyard(player1, "Lingering Tormentor");
    }
}
