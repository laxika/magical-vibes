package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonorablePassage.class, ProdigalPyromancer.class, RuneclawBear.class, GiantSpider.class,
        LightningBolt.class, ChandraNalaar.class})
class HonorablePassageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Honorable Passage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castPassage(player1);
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents a red source's damage and deals that much to its controller")
    void preventsRedSourceAndDamagesController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent giant = addCreatureReady(player2, new ProdigalPyromancer());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        // 1 combat damage prevented; Honorable Passage deals 1 to player2 (red source)
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents a non-red source's damage without damaging its controller")
    void preventsNonRedSourceWithoutDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent bears = addCreatureReady(player2, new RuneclawBear());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        bears.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the chosen red source's noncombat damage to a creature and damages its controller")
    void preventsNoncombatDamageToCreatureFromRedSource() {
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent victim = addCreatureReady(player1, new GiantSpider());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(0);
        harness.assertLife(player2, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the chosen red source's noncombat damage to a planeswalker and damages its controller")
    void preventsNoncombatDamageToPlaneswalkerFromRedSource() {
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player2, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen red spell on the stack to a creature and damages its controller")
    void preventsDamageFromRedSpellOnStackToCreature() {
        harness.setLife(player2, 20);
        Permanent victim = addCreatureReady(player1, new GiantSpider());
        LightningBolt lightningBolt = new LightningBolt();
        harness.setHand(player2, List.of(lightningBolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, victim.getId());
        harness.passPriority(player2);

        castPassage(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, lightningBolt.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(0);
        harness.assertLife(player2, 17);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents damage from a chosen red spell on the stack to a player and damages its controller")
    void preventsDamageFromRedSpellOnStackToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        LightningBolt lightningBolt = new LightningBolt();
        harness.setHand(player2, List.of(lightningBolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        castPassage(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, lightningBolt.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castPassage(player1);
        Permanent chosen = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent other = addCreatureReady(player2, new RuneclawBear());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(chosen.getId());
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castPassage(player1);
        Permanent giant = addCreatureReady(player2, new ProdigalPyromancer());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    private void castPassage(Player player) {
        harness.setHand(player, List.of(new HonorablePassage()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
