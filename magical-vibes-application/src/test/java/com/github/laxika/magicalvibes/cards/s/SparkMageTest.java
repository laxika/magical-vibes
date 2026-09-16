package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkMage.class, DwarvenGrunt.class})
class SparkMageTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger may deal 1 damage to a creature the damaged player controls")
    void combatDamageTriggerDealsDamageToDamagedPlayersCreature() {
        Permanent sparkMage = addCreatureReady(player1, new SparkMage());
        sparkMage.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new DwarvenGrunt());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new DwarvenGrunt());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(damagedPlayersCreature.getId()));

        assertThat(damagedPlayersCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining Spark Mage's combat damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent sparkMage = addCreatureReady(player1, new SparkMage());
        sparkMage.setAttacking(true);
        Permanent damagedPlayersCreature = addCreatureReady(player2, new DwarvenGrunt());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A blocked Spark Mage does not trigger")
    void blockedCombatDamageDoesNotTriggerAbility() {
        Permanent sparkMage = addCreatureReady(player1, new SparkMage());
        Permanent blocker = addCreatureReady(player2, new DwarvenGrunt());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(sparkMage))));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
