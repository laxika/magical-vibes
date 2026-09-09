package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Convalescence;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.p.Pandemonium;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoltariVisionary.class, Convalescence.class, ShieldMate.class, Pandemonium.class,
        GreaterAuramancy.class})
class SoltariVisionaryTest extends BaseCardTest {

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("When Soltari Visionary deals damage to a player, it prompts to destroy that player's enchantment")
    void promptsToDestroyDamagedPlayersEnchantment() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent convalescence = harness.addToBattlefieldAndReturn(player2, new Convalescence());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(convalescence.getId());
    }

    @Test
    @DisplayName("The chosen enchantment is destroyed")
    void destroysChosenEnchantment() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent convalescence = harness.addToBattlefieldAndReturn(player2, new Convalescence());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(convalescence.getId()));

        harness.assertNotOnBattlefield(player2, "Convalescence");
        harness.assertInGraveyard(player2, "Convalescence");
    }

    @Test
    @DisplayName("Only enchantments controlled by the damaged player can be chosen")
    void onlyDamagedPlayersEnchantments() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new Convalescence());
        Permanent enemyCreature = addCreatureReady(player2, new ShieldMate());
        Permanent enemyEnchantment = harness.addToBattlefieldAndReturn(player2, new Convalescence());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(enemyEnchantment.getId())
                .doesNotContain(ownEnchantment.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("No destroy choice is created when the damaged player controls no enchantments")
    void noTriggerWithoutEnchantments() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        addCreatureReady(player2, new ShieldMate());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Noncombat damage to a player also triggers the ability")
    void noncombatDamageToPlayerTriggersAbility() {
        harness.addToBattlefield(player1, new Pandemonium());
        Permanent convalescence = harness.addToBattlefieldAndReturn(player2, new Convalescence());

        harness.castFromHand(player1, new SoltariVisionary(), "{1}{W}{W}");
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(convalescence.getId());
    }

    @Test
    @DisplayName("A shrouded enchantment cannot be chosen")
    void shroudedEnchantmentCannotBeChosen() {
        Permanent visionary = addCreatureReady(player1, new SoltariVisionary());
        visionary.setAttacking(true);
        Permanent auramancy = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        Permanent protectedEnchantment = harness.addToBattlefieldAndReturn(player2, new Convalescence());

        assertThat(gqs.hasKeyword(gd, protectedEnchantment, Keyword.SHROUD)).isTrue();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsOnly(auramancy.getId());
    }
}
