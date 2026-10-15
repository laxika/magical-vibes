package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastbondOutcaster.class, AirElemental.class, HillGiant.class, GrizzlyBears.class})
class BeastbondOutcasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card when you control a creature with power 4 or greater")
    void drawsWithCreaturePowerFourOrGreater() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new BeastbondOutcaster(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Beastbond Outcaster");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when your creatures all have power less than 4")
    void doesNotDrawWithUnderpoweredCreature() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new BeastbondOutcaster(), "{2}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when only an opponent controls a creature with power 4 or greater")
    void doesNotDrawWithOpponentCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new BeastbondOutcaster(), "{2}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawIfQualifyingCreatureLeavesBeforeResolution() {
        var elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new BeastbondOutcaster(), "{2}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(elemental);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsWithPowerIncreasedByCounters() {
        var giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new BeastbondOutcaster(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void plotsAndCastsForFreeOnLaterTurnWithEntryTrigger() {
        var outcaster = new BeastbondOutcaster();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(outcaster));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(outcaster);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThatThrownBy(() -> harness.castFromExile(player1, outcaster.getId()))
                .hasMessageContaining("on the turn it became plotted");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromExile(player1, outcaster.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beastbond Outcaster");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(outcaster);
    }

    @Test
    void cannotPlotOutsideMainPhase() {
        harness.setHand(player1, List.of(new BeastbondOutcaster()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertInHand(player1, "Beastbond Outcaster");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
