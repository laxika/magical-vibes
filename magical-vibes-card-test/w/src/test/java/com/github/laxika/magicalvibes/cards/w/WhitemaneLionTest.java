package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.f.FrozenAether;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhitemaneLion.class, AvenRiftwatcher.class, FrozenAether.class})
class WhitemaneLionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a creature you control")
    void etbPromptsCreatureReturn() {
        harness.addToBattlefield(player1, new AvenRiftwatcher());
        castLion();

        GameData gd = harness.getGameData();
        UUID avenId = harness.getPermanentId(player1, "Aven Riftwatcher");
        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(avenId, lionId);
    }

    @Test
    @DisplayName("ETB excludes noncreatures and creatures controlled by an opponent")
    void etbOnlyOffersControlledCreatures() {
        harness.addToBattlefield(player1, new FrozenAether());
        harness.addToBattlefield(player2, new AvenRiftwatcher());
        castLion();

        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.validIds()).containsExactly(lionId);
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new AvenRiftwatcher());
        castLion();

        UUID avenId = harness.getPermanentId(player1, "Aven Riftwatcher");
        harness.handlePermanentChosen(player1, avenId);

        harness.assertInHand(player1, "Aven Riftwatcher");
        harness.assertOnBattlefield(player1, "Whitemane Lion");
    }

    @Test
    @DisplayName("A controlled creature returns to its owner's hand")
    void chosenCreatureReturnsToOwnerHand() {
        AvenRiftwatcher aven = new AvenRiftwatcher();
        aven.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, aven);
        castLion();

        UUID avenId = harness.getPermanentId(player1, "Aven Riftwatcher");
        harness.handlePermanentChosen(player1, avenId);

        harness.assertInHand(player2, "Aven Riftwatcher");
        harness.assertNotInHand(player1, "Aven Riftwatcher");
    }

    @Test
    @DisplayName("The Lion itself can be returned")
    void sourceCanBeReturned() {
        castLion();

        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        harness.handlePermanentChosen(player1, lionId);

        harness.assertInHand(player1, "Whitemane Lion");
        harness.assertNotOnBattlefield(player1, "Whitemane Lion");
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castLion();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        harness.handlePermanentChosen(player1, lionId);

        harness.assertInHand(player1, "Whitemane Lion");
        harness.assertNotOnBattlefield(player1, "Whitemane Lion");
    }

    @Test
    @DisplayName("The return trigger survives its source leaving and chooses creatures at resolution")
    void returnTriggerResolvesAfterSourceLeaves() {
        harness.castFromHand(player1, new WhitemaneLion(), "{1}{W}");
        WhitemaneLion secondLion = new WhitemaneLion();
        harness.setHand(player1, List.of(secondLion));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        UUID firstLionId = harness.getPermanentId(player1, "Whitemane Lion");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, secondLion, "{1}{W}");
        resolveAllTriggers();
        UUID secondLionId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(firstLionId))
                .findFirst().orElseThrow().getId();
        harness.handlePermanentChosen(player1, firstLionId);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(secondLionId);
        harness.handlePermanentChosen(player1, secondLionId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Whitemane Lion");
        assertThat(gd.stack).isEmpty();
    }

    private void castLion() {
        harness.castFromHand(player1, new WhitemaneLion(), "{1}{W}");
        resolveAllTriggers();
    }
}
