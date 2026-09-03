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
}
