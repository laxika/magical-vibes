package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.b.BarrowWitches;
import com.github.laxika.magicalvibes.cards.c.CorridorMonitor;
import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForeverYoung;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.l.LovestruckBeast;
import com.github.laxika.magicalvibes.cards.r.RagingRedcap;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        HappilyEverAfter.class,
        BakeIntoAPie.class,
        BarrowWitches.class,
        CorridorMonitor.class,
        ForeverYoung.class,
        LovestruckBeast.class,
        RagingRedcap.class,
        WildwoodTracker.class,
        AirElemental.class,
        DemonOfDeathsGate.class,
        Forest.class,
        GrizzlyBears.class,
        LavaSpike.class,
        ShivanDragon.class,
        Shock.class,
        SolRing.class
})
class HappilyEverAfterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering gains 5 life and draws a card for each player")
    void enterGainsLifeAndDrawsForEachPlayer() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new HappilyEverAfter(), "{2}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(25);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Wins at upkeep with all three requirements met")
    void winsWhenAllRequirementsAreMet() {
        addWinningSetup(true);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger without all five colors among controlled permanents")
    void doesNotTriggerWithoutFiveColors() {
        addWinningSetup(false);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Rechecks all upkeep requirements when the trigger resolves")
    void rechecksRequirementsOnResolution() {
        addWinningSetup(true);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 19);

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void doesNotTriggerBelowStartingLife() {
        addEldWinningSetup();
        harness.setLife(player1, 19);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void doesNotTriggerWithOnlyFiveCardTypes() {
        addEldWinningSetup();
        harness.setGraveyard(player1, List.of(new BakeIntoAPie()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureSorceryFaceDoesNotCountInGraveyard() {
        addEldWinningSetup();
        harness.setGraveyard(player1, List.of(new BakeIntoAPie(), new LovestruckBeast()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        addEldWinningSetup();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void graveyardAndOpposingPermanentsDoNotSupplyMissingColor() {
        addEldWinningSetup();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof WildwoodTracker);
        harness.addToBattlefield(player2, new WildwoodTracker());
        harness.setGraveyard(player1, List.of(new BakeIntoAPie(), new ForeverYoung(), new WildwoodTracker()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingColorBeforeResolutionPreventsWin() {
        addEldWinningSetup();
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof WildwoodTracker);

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void losingCardTypeBeforeResolutionPreventsWin() {
        addEldWinningSetup();
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new BakeIntoAPie()));

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void commanderDoesNotTriggerBelowFortyLife() {
        gd.format = DeckFormat.COMMANDER;
        addEldWinningSetup();
        harness.setLife(player1, 39);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void commanderRechecksStartingLifeOnResolution() {
        gd.format = DeckFormat.COMMANDER;
        addEldWinningSetup();
        harness.setLife(player1, 40);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 39);

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void winsAtCommanderStartingLife() {
        gd.format = DeckFormat.COMMANDER;
        addEldWinningSetup();
        harness.setLife(player1, 40);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private void addEldWinningSetup() {
        harness.addToBattlefield(player1, new HappilyEverAfter());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CorridorMonitor());
        harness.addToBattlefield(player1, new BarrowWitches());
        harness.addToBattlefield(player1, new RagingRedcap());
        harness.addToBattlefield(player1, new WildwoodTracker());
        harness.setGraveyard(player1, List.of(new BakeIntoAPie(), new ForeverYoung()));
    }

    private void addWinningSetup(boolean includeGreenPermanent) {
        harness.addToBattlefield(player1, new HappilyEverAfter());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        harness.addToBattlefield(player1, new ShivanDragon());
        if (includeGreenPermanent) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setGraveyard(player1, List.of(new Shock(), new LavaSpike()));
    }
}
