package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunBlessedHealer.class, GrizzlyBears.class, HillGiant.class, Forest.class,
        HolyDay.class, MindStone.class, Ornithopter.class, Shock.class})
class SunBlessedHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not return a card")
    void withoutKickerDoesNotReturnCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast(false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("When kicked, the ETB ability returns a target nonland permanent with mana value 2 or less")
    void kickedReturnsTargetPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The kicked ETB ability only allows nonland permanents with mana value 2 or less")
    void kickedFiltersIllegalGraveyardCards() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant();
        Forest forest = new Forest();
        HolyDay holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(bears, hillGiant, forest, holyDay));
        cast(true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    void kickedRequiresExactlyOneTargetFromYourGraveyardIncludingNoncreaturePermanents() {
        MindStone stone = new MindStone();
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(stone, bears));
        harness.setGraveyard(player2, List.of(opposingBears));
        cast(true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(stone.getId(), bears.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(stone.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void kickedReturnsZeroManaValuePermanent() {
        Ornithopter thopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(thopter));
        cast(true);
        harness.handleMultipleCardsChosen(player1, List.of(thopter.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    void kickedWithNoLegalTargetsStillEntersWithoutATriggerOnStack() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new Forest(), new HolyDay()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        cast(true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sun-Blessed Healer");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void kickedOffersOnlyLegalNonlandPermanentTargets() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new HillGiant(), new Forest(), new HolyDay()));
        cast(true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void kickedTriggerResolvesAfterHealerDies() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        cast(true);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Sun-Blessed Healer"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sun-Blessed Healer");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void targetLeavingGraveyardDoesNotCauseAnotherCardToBeReturned() {
        GrizzlyBears bears = new GrizzlyBears();
        MindStone stone = new MindStone();
        harness.setGraveyard(player1, List.of(bears, stone));
        cast(true);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of(stone));
        gd.getPlayerExiledCards(player1.getId()).add(bears);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast(boolean kicked) {
        if (kicked) {
            harness.setHand(player1, List.of(new SunBlessedHealer()));
            harness.addMana(player1, ManaColor.WHITE, 4);
            harness.castKickedCreature(player1, 0);
        } else {
            harness.castFromHand(player1, new SunBlessedHealer(), "{1}{W}");
        }
        harness.passBothPriorities();
    }
}
