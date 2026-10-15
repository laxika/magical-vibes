package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterReunion.class, ArgothianSprite.class, Forest.class, Mountain.class})
class BitterReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Entering offers a discard and then draws two cards")
    void acceptsDiscardAndDrawsTwo() {
        Card firstDraw = new Forest();
        Card secondDraw = new Mountain();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BitterReunion(), new ArgothianSprite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Argothian Sprite");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Declining the entering ability does not discard or draw")
    void declinesDiscardAndDraw() {
        Card topCard = new Forest();
        Card keptCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new BitterReunion(), keptCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing grants haste to your creatures until end of turn")
    void sacrificeGrantsHasteToOwnCreatures() {
        harness.addToBattlefield(player1, new BitterReunion());
        Permanent ownCreature = addCreatureReady(player1, new ArgothianSprite());
        Permanent opponentCreature = addCreatureReady(player2, new ArgothianSprite());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bitter Reunion");
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw")
    void emptyHandDoesNotDraw() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BitterReunion(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and haste waits for resolution")
    void hasteAppliesOnlyToCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new BitterReunion());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Bitter Reunion");
        harness.assertInGraveyard(player1, "Bitter Reunion");
        assertThat(existing.hasKeyword(Keyword.HASTE)).isFalse();

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.passBothPriorities();

        assertThat(existing.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.HASTE)).isTrue();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        assertThat(afterResolution.hasKeyword(Keyword.HASTE)).isFalse();
    }
}