package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvendoBrushrazer.class, Forest.class})
class EvendoBrushrazerTest extends BaseCardTest {

    @Test
    void sacrificesLandExilesTopCardAndAllowsPlayingItThisTurn() {
        Permanent brushrazer = addCreatureReady(player1, new EvendoBrushrazer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.getCardsExiledByPermanent(brushrazer.getId())).containsExactly(topCard);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .contains(topCard.getId());

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(brushrazer.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == topCard);

        harness.forceActivePlayer(player2);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player2.getId()))
                .doesNotContain(topCard.getId());
    }

    @Test
    void sacrificingTokenDoesNotExileTopCardOrGrantPermission() {
        addCreatureReady(player1, new EvendoBrushrazer());
        Card tokenLand = new Forest();
        tokenLand.setToken(true);
        harness.addToBattlefieldAndReturn(player1, tokenLand);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .doesNotContain(topCard.getId());
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
