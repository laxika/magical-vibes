package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpalineBracersTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new OpalineBracers()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bracers = findBracers(player1);
        assertThat(bracers.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void equippedCreatureGetsPlusOnePlusOneForEachChargeCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bracers = new Permanent(new OpalineBracers());
        bracers.setCounterCount(CounterType.CHARGE, 3);
        bracers.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(bracers);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void equipAttachesAndUsesCurrentChargeCounterCount() {
        Permanent bracers = addBracersReady(player1);
        bracers.setCounterCount(CounterType.CHARGE, 2);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private Permanent findBracers(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof OpalineBracers)
                .findFirst()
                .orElseThrow();
    }

    private Permanent addBracersReady(Player player) {
        Permanent permanent = new Permanent(new OpalineBracers());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
