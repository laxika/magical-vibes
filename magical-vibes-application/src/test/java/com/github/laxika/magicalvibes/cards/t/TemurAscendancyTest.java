package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemurAscendancy.class, WetlandSambar.class, AlpineGrizzly.class})
class TemurAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls")
    void grantsHasteToOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.addToBattlefield(player1, new WetlandSambar());
        harness.addToBattlefield(player2, new WetlandSambar());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Wetland Sambar"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Wetland Sambar"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("May draw when a creature with power 4 or greater enters under its controller's control")
    void mayDrawForHighPowerCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.castFromHand(player1, new AlpineGrizzly(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for a creature with power less than 4")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.castFromHand(player1, new WetlandSambar(), "{1}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player2, new AlpineGrizzly());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller may decline the draw")
    void mayDeclineDraw() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        harness.enterBattlefieldAndReturn(player1, new AlpineGrizzly());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ForceAway.class})
    @DisplayName("Draw trigger resolves even after the entering creature leaves")
    void drawsAfterEnteringCreatureLeaves() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setLibrary(player1, List.of(new WetlandSambar()));
        var creature = harness.enterBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new ForceAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Alpine Grizzly")).isZero();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
