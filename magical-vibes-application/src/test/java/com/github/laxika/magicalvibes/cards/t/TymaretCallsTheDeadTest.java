package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
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

@CardUsed({TymaretCallsTheDead.class, Forest.class, Gravecrawler.class, OmenOfTheSea.class})
class TymaretCallsTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three and can exile a milled creature or enchantment for a Zombie")
    void chapterIMillsAndCreatesZombie() {
        Gravecrawler milledCreature = new Gravecrawler();
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
        Gravecrawler ownCreature = new Gravecrawler();
        Gravecrawler opponentCreature = new Gravecrawler();
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
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.setLibrary(player1, List.of(new Forest(), new OmenOfTheSea()));
        gd.playerLifeTotals.put(player1.getId(), 20);

        advanceToNextChapter();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Tymaret Calls the Dead"));
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TymaretCallsTheDead());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
