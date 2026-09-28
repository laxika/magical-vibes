package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteOrchidPhantom.class, GhostQuarter.class, Forest.class})
class WhiteOrchidPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a nonbasic land and its controller may fetch a tapped basic land")
    void destroysNonbasicLandAndFetchesTappedBasicLand() {
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.setLibrary(player2, List.of(new Forest()));

        castWhiteOrchidPhantom();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(nonbasicLand.getId()).doesNotContain(basicLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, basicLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost Quarter");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
    }

    @Test
    @DisplayName("ETB can resolve without choosing a target")
    void canResolveWithoutTarget() {
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWhiteOrchidPhantom() {
        harness.setHand(player1, List.of(new WhiteOrchidPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
