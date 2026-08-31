package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingThragg.class, CrawWurm.class})
class SnappingThraggTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger may deal 3 damage to a creature the damaged player controls")
    void combatDamageTriggerDealsDamageToDamagedPlayersCreature() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new CrawWurm());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the combat damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new CrawWurm());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
    }
}
