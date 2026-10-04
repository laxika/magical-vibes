package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FoundingOfOmashu.class, FrogSquirrels.class})
class FoundingOfOmashuTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates two Ally tokens")
    void chapterICreatesTwoAllyTokens() {
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    @DisplayName("Chapter II may discard a card and draw a card")
    void chapterIILootsWhenAccepted() {
        harness.setHand(player1, List.of(new FrogSquirrels()));
        harness.setLibrary(player1, List.of(new FrogSquirrels(), new FrogSquirrels()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III gives creatures you control +1/+0 until end of turn")
    void chapterIIIBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new FrogSquirrels());
        Permanent opponentCreature = addCreatureReady(player2, new FrogSquirrels());
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Casting the Saga triggers chapter I on entry")
    void castingSagaCreatesTokensOnEntry() {
        harness.setHand(player1, List.of(new FoundingOfOmashu()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "Founding of Omashu");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Ally")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
        assertThat(findPermanents(player2, "Ally")).isEmpty();
        for (Permanent token : findPermanents(player1, "Ally")) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        }
    }

    @Test
    @DisplayName("Declining chapter II neither discards nor draws")
    void chapterIIDoesNothingWhenDeclined() {
        harness.setHand(player1, List.of(new FrogSquirrels()));
        harness.setLibrary(player1, List.of(new FrogSquirrels(), new FrogSquirrels()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter II cannot draw with an empty hand")
    void chapterIICannotDrawWithoutDiscarding() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FrogSquirrels(), new FrogSquirrels()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter III leaves the Saga until resolution and affects only existing creatures")
    void finalChapterSacrificesSagaAndDoesNotBoostLaterCreatures() {
        Permanent existingCreature = addCreatureReady(player1, new FrogSquirrels());
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        assertThat(existingCreature.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(existingCreature.getPowerModifier()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Founding of Omashu");
        harness.assertInGraveyard(player1, "Founding of Omashu");
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());
        assertThat(laterCreature.getPowerModifier()).isZero();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FoundingOfOmashu());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
