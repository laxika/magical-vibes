package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.ChandraFlamecaller;
import com.github.laxika.magicalvibes.cards.p.ProphetOfDistortion;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZadasCommando.class, ProphetOfDistortion.class, ChandraFlamecaller.class})
class ZadasCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and deals 1 damage to an opponent")
    void cohortDealsDamageToOpponent() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(commando.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally")
    void cohortRequiresAnotherUntappedAlly() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent nonAlly = addCreatureReady(player1, new ProphetOfDistortion());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(commando.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cohort cannot target its controller")
    void cohortCannotTargetController() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(commando.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    void cohortCanTapSummoningSickAlly() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new ZadasCommando());

        harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(commando.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    void summoningSickCommandoCannotActivateCohort() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(commando.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    void cohortCannotTapAlreadyTappedAlly() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());
        ally.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(commando.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void cohortCannotTapOpponentsAlly() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player2, new ZadasCommando());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(commando.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    void cohortCanTargetOpponentsPlaneswalker() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraFlamecaller());
        chandra.setCounterCount(CounterType.LOYALTY, 4);

        harness.activateAbility(player1, battlefieldIndex(commando), 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
        assertThat(commando.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    void cohortCanTargetControllersPlaneswalker() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        addCreatureReady(player1, new ZadasCommando());
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraFlamecaller());
        chandra.setCounterCount(CounterType.LOYALTY, 4);

        harness.activateAbility(player1, battlefieldIndex(commando), 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 20);
    }

    @Test
    void cohortCannotTargetCreature() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent ally = addCreatureReady(player1, new ZadasCommando());
        Permanent target = addCreatureReady(player2, new ProphetOfDistortion());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(commando), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(commando.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
    }

    @Test
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent commando = addCreatureReady(player1, new ZadasCommando());
        Permanent blocker = addCreatureReady(player2, new ProphetOfDistortion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(commando);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        harness.assertLife(player2, 20);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
