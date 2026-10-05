package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightmareUnmaking.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class NightmareUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Greater-power mode exiles creatures above the caster's hand size")
    void exilesCreaturesWithPowerGreaterThanHandSize() {
        addCreatures();

        cast(0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Air Elemental");
    }

    @Test
    @DisplayName("Less-power mode exiles creatures below the caster's hand size")
    void exilesCreaturesWithPowerLessThanHandSize() {
        addCreatures();

        cast(1);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    private void addCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
    }

    @Test
    void greaterModeWithEmptyHandExilesCreaturesOnBothBattlefieldsButNotLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new NightmareUnmaking()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).contains("Air Elemental");
    }

    @Test
    void lessModeWithEmptyHandLeavesPositivePowerCreatures() {
        addCreatures();
        harness.setHand(player1, List.of(new NightmareUnmaking()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void greaterModeUsesHandSizeAtResolution() {
        addCreatures();
        harness.setHand(player1, List.of(
                new NightmareUnmaking(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactlyInAnyOrder("Hill Giant", "Air Elemental");
    }

    @Test
    void lessModeUsesCastersCurrentHandForBothBattlefields() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCreatures();
        harness.setHand(player1, List.of(new NightmareUnmaking(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 1);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(
                new NightmareUnmaking(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, modeIndex);
    }
}
