package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevotedGrafkeeper.class, DepartedSoulkeeper.class, Forest.class,
        StormriderSpirit.class, TimberlandGuide.class})
class DevotedGrafkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and mills two cards")
    void entersAndMillsTwoCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting a spell from the graveyard taps an opposing creature")
    void graveyardSpellCastTapsOpposingCreature() {
        harness.addToBattlefield(player1, new DevotedGrafkeeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.setGraveyard(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Disturb casts Devoted Grafkeeper transformed as Departed Soulkeeper")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent soulkeeper = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(soulkeeper.isTransformed()).isTrue();
        assertThat(soulkeeper.getCard()).isInstanceOf(DepartedSoulkeeper.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Departed Soulkeeper can block flying but not nonflying creatures")
    void backFaceBlocksOnlyFlyingCreatures() {
        Permanent soulkeeper = harness.addToBattlefieldAndReturn(player2, new DepartedSoulkeeper());
        soulkeeper.setSummoningSick(false);

        Permanent flyingAttacker = harness.addToBattlefieldAndReturn(player1, new StormriderSpirit());
        flyingAttacker.setSummoningSick(false);
        flyingAttacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(soulkeeper.isBlocking()).isTrue();

        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent groundAttacker = harness.addToBattlefieldAndReturn(player1, new TimberlandGuide());
        groundAttacker.setSummoningSick(false);
        groundAttacker.setAttacking(true);
        soulkeeper.setBlocking(false);

        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Departed Soulkeeper is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGraveyard() {
        DevotedGrafkeeper card = new DevotedGrafkeeper();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        Permanent soulkeeper = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, soulkeeper));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .containsExactly(card.getId());
    }

    @Test
    @DisplayName("Milling a one-card library mills only that card")
    void millsRemainingCardOfShortLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Casting from hand does not trigger the tap ability")
    void handSpellDoesNotTapOpposingCreature() {
        harness.addToBattlefield(player1, new DevotedGrafkeeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.setHand(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's graveyard spell does not trigger the tap ability")
    void opponentGraveyardSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DevotedGrafkeeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new DevotedGrafkeeper()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFlashback(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The front face goes to the graveyard normally")
    void frontFaceIsNotExiledWhenItDies() {
        DevotedGrafkeeper card = new DevotedGrafkeeper();
        Permanent grafkeeper = harness.addToBattlefieldAndReturn(player1, card);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, grafkeeper));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The tap trigger cannot target a creature you control")
    void tapTriggerRejectsOwnCreature() {
        Permanent grafkeeper = harness.addToBattlefieldAndReturn(player1, new DevotedGrafkeeper());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TimberlandGuide());
        harness.setGraveyard(player1, List.of(new DevotedGrafkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, grafkeeper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(grafkeeper.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(opponentCreature.isTapped()).isTrue();
        harness.passBothPriorities();
    }

}
