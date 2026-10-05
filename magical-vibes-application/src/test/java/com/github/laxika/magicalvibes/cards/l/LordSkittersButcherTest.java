package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordSkittersButcher.class, Forest.class, GrizzlyBears.class, Island.class})
class LordSkittersButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Rat token that can't block")
    void createsRatTokenThatCantBlock() {
        castButcher(0);

        Permanent rat = findPermanent(player1, "Rat");
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing another creature scries 2, then draws a card")
    void sacrificesAnotherCreatureScriesAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawCard = new Island();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), drawCard));
        castButcher(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).hasSize(2);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("The sacrifice mode can't sacrifice Lord Skitter's Butcher itself")
    void sacrificeModeRequiresAnotherCreature() {
        castButcher(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(findPermanent(player1, "Lord Skitter's Butcher")).isNotNull();
    }

    @Test
    @DisplayName("The menace mode affects all creatures you control until end of turn")
    void menaceModeAffectsAllCreaturesUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castButcher(2);

        Permanent butcher = findPermanent(player1, "Lord Skitter's Butcher");
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, butcher, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Declining the sacrifice does not scry or draw")
    void decliningSacrificeDoesNotScryOrDraw() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LordSkittersButcher());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        castButcher(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLibraries.get(player1.getId())).hasSize(2).first().isSameAs(topCard);
    }

    @Test
    @DisplayName("Without another controlled creature the sacrifice mode does not scry or draw")
    void cannotSacrificeOpponentsCreatureOrOwnLand() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LordSkittersButcher());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        castButcher(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLibraries.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Menace affects only controlled creatures present when the ability resolves")
    void menaceExcludesOpponentsLandsAndLaterCreatures() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LordSkittersButcher());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castButcher(2);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new LordSkittersButcher());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lord Skitter's Butcher"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.MENACE)).isFalse();
    }

    private void castButcher(int mode) {
        harness.setHand(player1, List.of(new LordSkittersButcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0, mode);
        resolveAllTriggers();
    }
}
