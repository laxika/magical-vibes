package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lich.class, DuskImp.class, FlameBurst.class})
class LichTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield loses the controller's life total")
    void enteringTheBattlefieldLosesLifeTotal() {
        harness.castFromHand(player1, new Lich(), "{B}{B}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Life gain is replaced by drawing cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new Lich());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Damage requires sacrificing nontoken permanents and allows choosing which ones")
    void damageRequiresSacrificingNontokenPermanents() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        Permanent firstImp = addCreatureReady(player1, new DuskImp());
        Permanent secondImp = addCreatureReady(player1, new DuskImp());
        DuskImp tokenCard = new DuskImp();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);

        dealTwoDamageToPlayer1();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstImp.getId(), secondImp.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lich, token)
                .doesNotContain(firstImp, secondImp);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Insufficient nontoken permanents cause the controller to lose")
    void insufficientNontokenPermanentsCauseLoss() {
        harness.addToBattlefield(player1, new Lich());
        DuskImp tokenCard = new DuskImp();
        tokenCard.setToken(true);
        harness.addToBattlefield(player1, tokenCard);

        dealTwoDamageToPlayer1();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Putting Lich into a graveyard makes its controller lose")
    void puttingLichIntoGraveyardCausesLoss() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new Lich());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void dealTwoDamageToPlayer1() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
    }
}
