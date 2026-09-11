package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnidentifiedHovership.class, GrizzlyBears.class, ColossalDreadmaw.class,
        Shatter.class, Forest.class})
class UnidentifiedHovershipTest extends BaseCardTest {

    @Test
    @DisplayName("The enter trigger exiles up to one target creature with toughness 5 or less")
    void enterTriggerExilesEligibleCreature() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tooTough = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tooTough.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, eligible.getId());
        harness.passBothPriorities();

        Permanent hovership = findPermanent(player1, "Unidentified Hovership");
        assertThat(gd.getCardsExiledByPermanent(hovership.getId()))
                .extracting(Card::getId)
                .containsExactly(eligible.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("When it leaves, the exiled card's owner manifests dread")
    void leavesTriggerUsesExiledCardsOwnerAndPutsTheOtherCardInTheirGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnidentifiedHovership()));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        addHovershipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent hovership = findPermanent(player1, "Unidentified Hovership");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hovership.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(graveyardCard);
    }

    private void addHovershipMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
