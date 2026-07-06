package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SnoopingPageTest extends BaseCardTest {

    private Permanent addReadyPage(Player player) {
        Permanent perm = new Permanent(new SnoopingPage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    // ===== Repartee: can't be blocked this turn =====

    @Test
    @DisplayName("Casting an instant that targets a creature makes the Page unblockable this turn")
    void reparteeMakesUnblockable() {
        Permanent page = addReadyPage(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // resolve Repartee trigger

        assertThat(page.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable resets at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent page = addReadyPage(player1);
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(page.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(page.isCantBeBlocked()).isFalse();
    }

    // ===== Combat damage trigger: draw + lose 1 life =====

    @Test
    @DisplayName("Dealing combat damage to a player draws a card and loses 1 life")
    void combatDamageDrawsAndLosesLife() {
        Permanent page = addReadyPage(player1);
        page.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Page is 2/3 — player2 takes 2 combat damage
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);

        // Resolve the two triggered abilities (draw, lose 1 life)
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        addReadyPage(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
}
