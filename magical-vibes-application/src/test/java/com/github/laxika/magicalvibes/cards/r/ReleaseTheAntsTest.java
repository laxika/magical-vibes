package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReleaseTheAnts.class, ElvishWarrior.class, MurmuringBosk.class})
class ReleaseTheAntsTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ReleaseTheAnts()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    // Caster (player1) wins the clash: their top card (Elvish Warrior MV 2) beats the opponent's Murmuring Bosk (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new MurmuringBosk(), new MurmuringBosk()));
        harness.setLibrary(player2, List.of(new MurmuringBosk(), new MurmuringBosk(), new MurmuringBosk()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new MurmuringBosk(), new MurmuringBosk(), new MurmuringBosk()));
        harness.setLibrary(player2, List.of(new ElvishWarrior(), new MurmuringBosk(), new MurmuringBosk()));
    }

    private void stackClashTieForCaster() {
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new MurmuringBosk(), new MurmuringBosk()));
        harness.setLibrary(player2, List.of(new ElvishWarrior(), new MurmuringBosk(), new MurmuringBosk()));
    }

    private void keepBothRevealedCardsOnTop() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Deals 1 damage to a target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        stackClashLossForCaster();
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        keepBothRevealedCardsOnTop();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void deals1DamageToCreature() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        stackClashLossForCaster();
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Elvish Warrior");
        harness.castAndResolveInstant(player1, 0, targetId);

        keepBothRevealedCardsOnTop();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(targetId) && p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Winning the clash returns Release the Ants to its owner's hand")
    void wonClashReturnsSpellToHand() {
        stackClashWinForCaster();
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        keepBothRevealedCardsOnTop();

        harness.assertInHand(player1, "Release the Ants");
        harness.assertNotInGraveyard(player1, "Release the Ants");
    }

    @Test
    @DisplayName("Losing the clash sends Release the Ants to the graveyard")
    void lostClashSendsSpellToGraveyard() {
        stackClashLossForCaster();
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        keepBothRevealedCardsOnTop();

        harness.assertInGraveyard(player1, "Release the Ants");
        harness.assertNotInHand(player1, "Release the Ants");
    }

    @Test
    @DisplayName("A tied clash sends Release the Ants to the graveyard")
    void tiedClashSendsSpellToGraveyard() {
        stackClashTieForCaster();
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        keepBothRevealedCardsOnTop();

        harness.assertInGraveyard(player1, "Release the Ants");
        harness.assertNotInHand(player1, "Release the Ants");
    }

    @Test
    @DisplayName("The active opponent chooses placement before the nonactive caster")
    void activeOpponentChoosesFirst() {
        stackClashWinForCaster();
        prepare();
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertLife(player2, 19);
        harness.assertInHand(player1, "Release the Ants");
    }

    @Test
    @DisplayName("Bottoming the winning card does not change the clash result")
    void bottomingWinningCardStillReturnsSpell() {
        ElvishWarrior revealed = new ElvishWarrior();
        MurmuringBosk next = new MurmuringBosk();
        harness.setLibrary(player1, List.of(revealed, next));
        harness.setLibrary(player2, List.of(new MurmuringBosk()));
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, revealed);
        harness.assertInHand(player1, "Release the Ants");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An illegal sole target prevents damage, clash, and return to hand")
    void illegalTargetPreventsClash() {
        var target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        ElvishWarrior top = new ElvishWarrior();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(new MurmuringBosk()));
        prepare();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Release the Ants");
        harness.assertNotInHand(player1, "Release the Ants");
    }

    @Test
    @DisplayName("Revealing a land wins against an opponent with an empty library")
    void revealedLandWinsAgainstEmptyLibrary() {
        harness.setLibrary(player1, List.of(new MurmuringBosk()));
        harness.setLibrary(player2, List.of());
        prepare();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInHand(player1, "Release the Ants");
        harness.assertLife(player2, 19);
    }
}
