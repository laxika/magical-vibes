package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeflectingPalmTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Deflecting Palm prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castPalm(player1);
        addReady(player2, new GoblinPiker());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen source's damage and damages its controller")
    void preventsDamageAndDamagesSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPalm(player1);
        Permanent goblin = addReady(player2, new GoblinPiker());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevents the chosen source's noncombat damage and damages its controller")
    void preventsNoncombatDamageAndDamagesSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPalm(player1);
        Permanent piker = addReady(player2, new ProdigalPyromancer());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, piker.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, piker), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A different source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castPalm(player1);
        Permanent chosen = addReady(player2, new GoblinPiker());
        Permanent other = addReady(player2, new GoblinPiker());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    private void castPalm(Player player) {
        harness.setHand(player, List.of(new DeflectingPalm()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castInstant(player, 0);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
