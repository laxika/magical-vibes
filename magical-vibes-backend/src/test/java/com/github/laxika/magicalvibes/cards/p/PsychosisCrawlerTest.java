package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychosisCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Psychosis Crawler has correct effects registered")
    void hasCorrectEffects() {
        PsychosisCrawler card = new PsychosisCrawler();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCardsInHandEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS).getFirst())
                .isInstanceOf(EachOpponentLosesLifeEffect.class);
        EachOpponentLosesLifeEffect drawEffect =
                (EachOpponentLosesLifeEffect) card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS).getFirst();
        assertThat(drawEffect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent crawler = addCrawlerReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        // Add cards to hand
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Psychosis Crawler dies with empty hand (0/0)")
    void diesWithEmptyHand() {
        gd.playerHands.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new PsychosisCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve cast — enters as 0/0, SBA kills it

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Psychosis Crawler"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychosis Crawler"));
    }

    @Test
    @DisplayName("P/T updates dynamically as hand size changes")
    void ptUpdatesDynamically() {
        Permanent crawler = addCrawlerReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent crawler = addCrawlerReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draw step draw triggers each opponent loses 1 life")
    void triggersOnDrawStepDraw() {
        harness.addToBattlefield(player1, new PsychosisCrawler());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears()); // keep alive
        harness.setLife(player2, 20);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Psychosis Crawler trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Drawing from a spell triggers each opponent loses 1 life per card drawn")
    void triggersOnSpellDraw() {
        harness.addToBattlefield(player1, new PsychosisCrawler());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears()); // keep alive
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Counsel of the Soratami draws 2 cards
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel of the Soratami (draws 2)
        harness.passBothPriorities(); // resolve first Psychosis Crawler trigger
        harness.passBothPriorities(); // resolve second Psychosis Crawler trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent's draw does not trigger Psychosis Crawler")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new PsychosisCrawler());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears()); // keep alive
        harness.setLife(player2, 20);

        advanceToDraw(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    private Permanent addCrawlerReady(Player player) {
        PsychosisCrawler card = new PsychosisCrawler();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
