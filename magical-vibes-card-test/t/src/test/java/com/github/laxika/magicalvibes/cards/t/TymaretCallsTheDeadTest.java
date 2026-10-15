package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DiscordantPiper;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TymaretCallsTheDead.class, Forest.class, DiscordantPiper.class, OmenOfTheSea.class})
class TymaretCallsTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three and can exile a milled creature or enchantment for a Zombie")
    void chapterIMillsAndCreatesZombie() {
        DiscordantPiper milledCreature = new DiscordantPiper();
        OmenOfTheSea milledEnchantment = new OmenOfTheSea();
        Forest milledLand = new Forest();
        harness.setLibrary(player1, List.of(milledCreature, milledEnchantment, milledLand));
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                milledCreature.getId(), milledEnchantment.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(milledCreature, milledEnchantment, milledLand);

        harness.handleMultipleCardsChosen(player1, List.of(milledCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(milledCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(milledEnchantment, milledLand);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Chapter I only offers own creature or enchantment cards and may be declined")
    void chapterIExileMayBeDeclined() {
        DiscordantPiper ownCreature = new DiscordantPiper();
        DiscordantPiper opponentCreature = new DiscordantPiper();
        Forest ownLandOne = new Forest();
        Forest ownLandTwo = new Forest();
        harness.setLibrary(player1, List.of(ownCreature, ownLandOne, ownLandTwo));
        harness.setGraveyard(player2, List.of(opponentCreature));
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(ownCreature, ownLandOne, ownLandTwo);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie")))
                .isEmpty();
    }

    @Test
    @DisplayName("Chapter III gains and scries for the number of Zombies controlled")
    void chapterIIIGainsLifeAndScriesForZombies() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new DiscordantPiper());
        harness.addToBattlefield(player1, new DiscordantPiper());
        harness.setLibrary(player1, List.of(new Forest(), new OmenOfTheSea()));
        harness.setLife(player1, 20);

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Tymaret Calls the Dead"));
    }

    @Test
    void enteringTriggersChapterIAndCanExileAnOlderEnchantment() {
        OmenOfTheSea enchantment = new OmenOfTheSea();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new TymaretCallsTheDead());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void chapterIICanExileAnEnchantmentFromAShortLibrary() {
        OmenOfTheSea enchantment = new OmenOfTheSea();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(enchantment, land));
        Permanent saga = addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void chapterIIWithoutEligibleCardsOnlyMills() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void chapterIIIWithOnlyOpposingZombiesGainsNothingAndDoesNotScry() {
        Permanent saga = addSagaWithLore(2);
        harness.addToBattlefield(player2, new DiscordantPiper());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TymaretCallsTheDead());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
