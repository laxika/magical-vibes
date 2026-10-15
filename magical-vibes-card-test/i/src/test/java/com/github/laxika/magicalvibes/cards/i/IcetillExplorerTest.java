package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcetillExplorer.class, Forest.class, GrizzlyBears.class})
class IcetillExplorerTest extends BaseCardTest {

    @Test
    void controllerMayPlayAnAdditionalLandAndPlayLandsFromGraveyard() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Icetill Explorer", "Forest", "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> "Forest".equals(card.getName()));
    }

    @Test
    void landfallMillsOneCardFromControllerLibrary() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void landPlayLimitStillAppliesAfterAdditionalLandPlay() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void milledLandCanBePlayedAsTheAdditionalLand() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        Forest milledLand = new Forest();
        harness.setLibrary(player1, List.of(milledLand, new IcetillExplorer()));
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of());

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledLand);

        harness.playGraveyardLand(player1, milledLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Icetill Explorer");
    }

    @Test
    void opponentsLandDoesNotTriggerMillOrGainAnAdditionalLandPlay() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.playLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleExplorersEachGrantAnAdditionalLandAndTriggerMill() {
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.addToBattlefield(player1, new IcetillExplorer());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        for (int i = 0; i < 3; i++) {
            harness.playLand(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void permissionsEndWhenExplorerLeavesButPendingLandfallStillResolves() {
        var explorer = harness.addToBattlefieldAndReturn(player1, new IcetillExplorer());
        harness.setLibrary(player1, List.of(new IcetillExplorer()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(explorer);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Icetill Explorer");
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void losingAllAbilitiesRemovesTheAdditionalLandAllowance() {
        var explorer = harness.addToBattlefieldAndReturn(player1, new IcetillExplorer());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        explorer.setLosesAllAbilitiesUntilEndOfTurn(true);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
