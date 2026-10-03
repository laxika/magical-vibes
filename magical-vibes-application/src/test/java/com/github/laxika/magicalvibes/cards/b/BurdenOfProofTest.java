package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurdenOfProof.class, NoviceInspector.class, GrizzlyBears.class})
class BurdenOfProofTest extends BaseCardTest {

    @Test
    void detectiveYouControlGetsPlusTwoPlusTwo() {
        Permanent detective = addCreatureReady(player1, new NoviceInspector());
        int basePower = gqs.getEffectivePower(gd, detective);
        int baseToughness = gqs.getEffectiveToughness(gd, detective);
        attachAura(player1, detective);

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(baseToughness + 2);
    }

    @Test
    void creatureThatIsNotYourDetectiveBecomesOneOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void enchantedCreatureCannotBlockDetectives() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, blocker);
        Permanent detective = addCreatureReady(player1, new NoviceInspector());

        assertThat(bls.canBlockAttacker(gd, blocker, detective,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void detectiveControlledByOpponentGetsOneOneAndCannotBlockDetectives() {
        Permanent detective = addCreatureReady(player2, new NoviceInspector());
        attachAura(player1, detective);
        Permanent otherDetective = addCreatureReady(player1, new NoviceInspector());

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(1);
        assertThat(bls.canBlockAttacker(gd, detective, otherDetective,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    void canCastDuringOpponentsUpkeepAndAttachToTheirCreature() {
        Permanent creature = addCreatureReady(player2, new NoviceInspector());
        harness.setHand(player1, List.of(new BurdenOfProof()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Burden of Proof").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void yourEnchantedDetectiveCanStillBlockDetectives() {
        Permanent blocker = addCreatureReady(player2, new NoviceInspector());
        attachAura(player2, blocker);
        Permanent attacker = addCreatureReady(player1, new NoviceInspector());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void enchantedCreatureCanBlockNonDetectives() {
        Permanent blocker = addCreatureReady(player2, new NoviceInspector());
        attachAura(player1, blocker);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    void changingAuraControllerSwitchesBothConditionalEffects() {
        Permanent blocker = addCreatureReady(player2, new NoviceInspector());
        Permanent aura = attachAura(player1, blocker);
        Permanent attacker = addCreatureReady(player1, new NoviceInspector());

        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(4);
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new BurdenOfProof());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
