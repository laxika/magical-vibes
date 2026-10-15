package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarkOfTheVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RashelFistOfTorm.class, GrizzlyBears.class, MarkOfTheVampire.class})
class RashelFistOfTormTest extends BaseCardTest {

    @Test
    void eachAuraYouControlHasExalted() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        attachAura(bears);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
    }

    @Test
    void exaltedDoesNotTriggerWhenMultipleCreaturesAttack() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachAura(bears);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 3));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    private Permanent attachAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkOfTheVampire());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }

    @Test
    void rashelDoesNotHaveExaltedWithoutAnAura() {
        Permanent rashel = addCreatureReady(player1, new RashelFistOfTorm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, rashel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rashel)).isEqualTo(4);
    }

    @Test
    void ownAuraOnOpponentsCreatureBoostsOwnAttacker() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        attachAura(enchanted);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(4);
    }

    @Test
    void opponentsAuraDoesNotGainExaltedFromRashel() {
        addCreatureReady(player1, new RashelFistOfTorm());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new MarkOfTheVampire());
        aura.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void queuedExaltedSurvivesRashelLeavingBattlefield() {
        Permanent rashel = addCreatureReady(player1, new RashelFistOfTorm());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachAura(attacker);

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(rashel);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }
}
