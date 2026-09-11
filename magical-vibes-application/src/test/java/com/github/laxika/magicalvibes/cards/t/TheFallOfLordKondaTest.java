package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FragmentOfKonda;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFallOfLordKonda.class, FragmentOfKonda.class, DoomBlade.class, Forest.class,
        GrizzlyBears.class, HillGiant.class})
class TheFallOfLordKondaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles only an opponent's creature with mana value 4 or greater")
    void chapterIExilesEligibleOpponentCreature() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentGiant.getId())
                .doesNotContain(ownGiant.getId(), opponentBears.getId());

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentGiant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentGiant.getCard());
    }

    @Test
    @DisplayName("Chapter II returns each permanent to its owner")
    void chapterIIReturnsPermanentsToOwners() {
        Permanent player1Permanent = addPermanentOwnedBy(player2, player1);
        Permanent player2Permanent = addPermanentOwnedBy(player1, player2);
        Permanent player2OwnPermanent = harness.addToBattlefieldAndReturn(player2, new Forest());

        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Permanent)
                .doesNotContain(player2Permanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Permanent, player2OwnPermanent)
                .doesNotContain(player1Permanent);
    }

    @Test
    @DisplayName("Chapter III transforms into Fragment of Konda, which draws when it dies")
    void chapterIIITransformsAndBackFaceDrawsOnDeath() {
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent fragment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FragmentOfKonda)
                .findFirst()
                .orElseThrow();
        assertThat(fragment.isTransformed()).isTrue();

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, fragment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fragment);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheFallOfLordKonda());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addPermanentOwnedBy(Player controller, Player owner) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, new Forest());
        permanent.getCard().setOwnerId(owner.getId());
        return permanent;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
