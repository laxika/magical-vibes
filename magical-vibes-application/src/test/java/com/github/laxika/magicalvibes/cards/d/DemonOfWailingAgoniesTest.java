package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonOfWailingAgonies.class, EdgarMarkov.class, GrizzlyBears.class})
class DemonOfWailingAgoniesTest extends BaseCardTest {

    @Test
    void lieutenantBoostsDemon() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent demon = addCreatureReady(player1, new DemonOfWailingAgonies());

        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(6);
    }

    @Test
    void noCommanderMeansNoLieutenantBonusOrTrigger() {
        Permanent demon = addCreatureReady(player1, new DemonOfWailingAgonies());
        demon.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(4);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enemyCreature);
    }

    @Test
    void lieutenantMakesDamagedPlayerSacrificeCreature() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        Permanent demon = addCreatureReady(player1, new DemonOfWailingAgonies());
        demon.setAttacking(true);
        Permanent firstEnemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondEnemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                firstEnemyCreature.getId(), secondEnemyCreature.getId());

        harness.handlePermanentChosen(player2, firstEnemyCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(secondEnemyCreature)
                .doesNotContain(firstEnemyCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
