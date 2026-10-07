package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HithlainRope;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TormentOfScarabs.class, Forest.class, GrizzlyBears.class})
class TormentOfScarabsTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Lose 3 life";

    @Test
    @DisplayName("Enchanted player with no hand and no nonland permanent just loses 3 life")
    void losesLifeWhenNoOtherOption() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A land does not count as a nonland permanent — player loses 3 life, land untouched")
    void landDoesNotSatisfySacrifice() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Enchanted player may choose to lose 3 life even with a permanent and a card")
    void mayChooseToLoseLife() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player2, LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted player may sacrifice a nonland permanent instead of losing life")
    void maySacrificeNonlandPermanent() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Enchanted player may discard a card instead of losing life")
    void mayDiscardACard() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Trigger does NOT fire during the curse controller's upkeep")
    void triggerDoesNotFireOnControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SongOfTheDryads.class})
    void permanentTurnedIntoLandCannotSatisfySacrifice() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent song = harness.addToBattlefieldAndReturn(player1, new SongOfTheDryads());
        song.setAttachedTo(bears.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({HithlainRope.class})
    void cannotSacrificePermanentWithSacrificeProhibition() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new HithlainRope());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Hithlain Rope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castingAttachesCurseToChosenPlayer() {
        harness.setHand(player1, List.of(new TormentOfScarabs()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Torment of Scarabs").getAttachedTo())
                .isEqualTo(player2.getId());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void enchantedControllerMaySacrificeCurseItself() {
        Permanent curse = placeCurseOnPlayer(player1, player1);
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player1, curse.getId());

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Torment of Scarabs");
        harness.assertInGraveyard(player1, "Torment of Scarabs");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void triggerResolvesAfterCurseLeavesBattlefield() {
        Permanent curse = placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Torment of Scarabs");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new TormentOfScarabs());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
