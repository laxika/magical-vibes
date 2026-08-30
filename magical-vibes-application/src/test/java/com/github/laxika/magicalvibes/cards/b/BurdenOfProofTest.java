package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

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

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new BurdenOfProof());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
