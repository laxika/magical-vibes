package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChimericEgg.class, CrazedGoblin.class, DarksteelIngot.class, ArcboundWorker.class})
class ChimericEggTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a nonartifact spell puts a charge counter on Chimeric Egg")
    void opponentNonartifactSpellAddsChargeCounter() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        castSpell(player2, new CrazedGoblin(), "{R}");

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent casting an artifact spell does not trigger Chimeric Egg")
    void opponentArtifactSpellDoesNotAddChargeCounter() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        castSpell(player2, new DarksteelIngot(), "{3}");

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Chimeric Egg does not trigger when its controller casts a nonartifact spell")
    void controllerNonartifactSpellDoesNotAddChargeCounter() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        castSpell(player1, new CrazedGoblin(), "{R}");

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void opponentArtifactCreatureSpellDoesNotAddChargeCounter() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        castSpell(player2, new ArcboundWorker(), "{1}");

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing three charge counters animates Chimeric Egg until end of turn")
    void activatesAnimation() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        egg.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.isArtifact(egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(6);
        assertThat(egg.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT);
        assertThat(egg.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, egg)).isFalse();
        assertThat(gqs.isArtifact(egg)).isTrue();
        assertThat(egg.getTransientSubtypes()).doesNotContain(CardSubtype.CONSTRUCT);
        assertThat(egg.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    void cannotActivateWithFewerThanThreeChargeCounters() {
        addCreatureReady(player1, new ChimericEgg()).setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chargeCounterTriggerResolvesBeforeTheSpell() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        castSpell(player2, new CrazedGoblin(), "{R}");

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void activationPaysCountersImmediatelyAndWorksWhileTappedAndSummoningSick() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        egg.setSummoningSick(true);
        egg.tap();
        egg.setCounterCount(CounterType.CHARGE, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, egg)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(6);
        assertThat(egg.isTapped()).isTrue();
        assertThat(egg.isSummoningSick()).isTrue();
        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void canActivateAgainWhileAlreadyAnimated() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        egg.setCounterCount(CounterType.CHARGE, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, egg)).isEqualTo(6);
        assertThat(egg.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void animatedEggStillTriggersForOpponentsNonartifactSpells() {
        Permanent egg = addCreatureReady(player1, new ChimericEgg());
        egg.setCounterCount(CounterType.CHARGE, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        castSpell(player2, new CrazedGoblin(), "{R}");
        harness.passBothPriorities();

        assertThat(egg.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, egg)).isTrue();
        assertThat(gqs.getEffectivePower(gd, egg)).isEqualTo(6);
    }

    private void castSpell(Player caster, Card spell, String manaCost) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(caster, spell, manaCost);
    }
}
