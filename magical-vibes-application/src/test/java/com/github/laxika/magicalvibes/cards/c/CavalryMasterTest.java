package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CavalryMaster.class, BenalishCavalry.class, AshcoatBear.class, DurkwoodBaloth.class})
class CavalryMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a flanking instance to other flanking creatures you control")
    void addsFlankingInstanceToOtherFlankingCreatures() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());

        assertThat(gqs.flankingInstances(gd, cavalry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not grant flanking to a creature without flanking")
    void doesNotGrantFlankingToNonFlankingCreature() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flanking to an opponent's creature")
    void doesNotGrantFlankingToOpponentsCreature() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent bear = addCreatureReady(player2, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("The granted flanking instance triggers separately")
    void grantedFlankingInstanceTriggersSeparately() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent attacker = addCreatureReady(player1, new BenalishCavalry());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());
        attacker.setAttacking(true);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, attackerIndex)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not grant an additional flanking instance to Cavalry Master itself")
    void doesNotGrantAdditionalFlankingToItself() {
        Permanent attacker = addCreatureReady(player1, new CavalryMaster());
        Permanent blocker = addCreatureReady(player2, new DurkwoodBaloth());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(4);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not add a flanking instance to an opponent's flanking creature")
    void doesNotEnhanceOpponentsFlankingCreature() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent cavalry = addCreatureReady(player2, new BenalishCavalry());

        assertThat(gqs.flankingInstances(gd, cavalry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Cavalry Masters grant separate instances and enhance each other")
    void multipleMastersGrantSeparateInstances() {
        Permanent first = addCreatureReady(player1, new CavalryMaster());
        Permanent second = addCreatureReady(player1, new CavalryMaster());
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());

        assertThat(gqs.flankingInstances(gd, first)).isEqualTo(2);
        assertThat(gqs.flankingInstances(gd, second)).isEqualTo(2);
        assertThat(gqs.flankingInstances(gd, cavalry)).isEqualTo(3);
    }

    @Test
    @DisplayName("The extra flanking instance disappears when Cavalry Master leaves")
    void extraInstanceDisappearsWhenMasterLeaves() {
        Permanent master = addCreatureReady(player1, new CavalryMaster());
        Permanent cavalry = addCreatureReady(player1, new BenalishCavalry());
        assertThat(gqs.flankingInstances(gd, cavalry)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(master);

        assertThat(gqs.flankingInstances(gd, cavalry)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocker with flanking is unaffected by every flanking instance")
    void flankingBlockerIsUnaffected() {
        addCreatureReady(player1, new CavalryMaster());
        Permanent attacker = addCreatureReady(player1, new BenalishCavalry());
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }
}
