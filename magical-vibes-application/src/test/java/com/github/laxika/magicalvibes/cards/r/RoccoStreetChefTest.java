package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoccoStreetChef.class, Forest.class, GrizzlyBears.class})
class RoccoStreetChefTest extends BaseCardTest {

    @Test
    void exilesTheTopCardOfEachPlayersLibraryAndGrantsPlayPermission() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new RoccoStreetChef()));
        Forest playerOneCard = new Forest();
        GrizzlyBears playerTwoCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(playerOneCard));
        harness.setLibrary(player2, List.of(playerTwoCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(playerOneCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(playerTwoCard);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(playerOneCard.getId(), player1.getId())
                .containsEntry(playerTwoCard.getId(), player2.getId());
    }

    @Test
    void createsACounterAndFoodWhenAnOpponentPlaysALandFromExile() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        Forest land = new Forest();
        gd.addToExile(player2.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player2, land.getId(), null, null);
        harness.handlePermanentChosen(player1, rocco.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Food")).isNotNull();
    }

    @Test
    void createsACounterAndFoodWhenAnOpponentCastsASpellFromExile() {
        Permanent rocco = addCreatureReady(player1, new RoccoStreetChef());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player2.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player2, spell.getId(), null, null);
        harness.handlePermanentChosen(player1, rocco.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(rocco.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Food")).isNotNull();
    }
}
