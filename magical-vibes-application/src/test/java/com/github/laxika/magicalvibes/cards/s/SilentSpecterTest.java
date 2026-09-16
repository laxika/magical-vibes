package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentSpecter.class, ElvishWarrior.class, GoblinSharpshooter.class})
class SilentSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard two chosen cards")
    void combatDamageMakesDamagedPlayerDiscardTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior())));
        Permanent specter = addCreatureReady(player1, new SilentSpecter());
        specter.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A blocked Silent Specter does not make the defending player discard")
    void blockedSpecterDoesNotTrigger() {
        List<Card> hand = new ArrayList<>(List.of(new ElvishWarrior(), new ElvishWarrior()));
        harness.setHand(player2, hand);
        Permanent specter = addCreatureReady(player1, new SilentSpecter());
        specter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsAll(hand);
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Silent Specter's discard ability")
    void noncombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new SilentSpecter());
        Permanent sharpshooter = addCreatureReady(player1, new GoblinSharpshooter());
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishWarrior(), new ElvishWarrior())));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sharpshooter),
                null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Morph casts Silent Specter face down and turns it face up for {3}{B}{B}")
    void morphsFaceDownAndTurnsFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SilentSpecter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent specter = findPermanent(player1, "Silent Specter");
        assertThat(specter.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(specter));
        harness.passBothPriorities();

        assertThat(specter.isFaceDown()).isFalse();
    }
}
