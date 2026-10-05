package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RafterDemon.class, AxebaneBeast.class})
class RafterDemonTest extends BaseCardTest {

    @Test
    @DisplayName("For spectacle, each opponent discards a card when Rafter Demon enters")
    void spectacleETBForcesEachOpponentToDiscard() {
        AxebaneBeast discarded = new AxebaneBeast();
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.setHand(player2, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Axebane Beast");
    }

    @Test
    @DisplayName("The normal cast does not make opponents discard")
    void normalCastDoesNotTriggerDiscard() {
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.setHand(player2, List.of(new AxebaneBeast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Axebane Beast");
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent chooses exactly one card and the controller keeps their hand")
    void opponentChoosesOneCard() {
        AxebaneBeast kept = new AxebaneBeast();
        AxebaneBeast discarded = new AxebaneBeast();
        AxebaneBeast controllersCard = new AxebaneBeast();
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RafterDemon(), controllersCard));
        harness.setHand(player2, List.of(kept, discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllersCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Spectacle resolves when the opponent has an empty hand")
    void spectacleWithEmptyOpponentHand() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rafter Demon");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without casting does not trigger discard even after opponent life loss")
    void enteringWithoutCastingDoesNotTriggerDiscard() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        AxebaneBeast opponentsCard = new AxebaneBeast();
        harness.setHand(player2, List.of(opponentsCard));

        harness.enterBattlefieldAndReturn(player1, new RafterDemon());

        harness.assertOnBattlefield(player1, "Rafter Demon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    @DisplayName("The controller's own life loss does not enable spectacle")
    void ownLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rafter Demon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying the normal cost after opponent life loss does not trigger discard")
    void normalCostAfterOpponentLifeLossDoesNotTriggerDiscard() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        AxebaneBeast opponentsCard = new AxebaneBeast();
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.setHand(player2, List.of(opponentsCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rafter Demon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    @DisplayName("Spectacle requires its higher cost rather than the normal mana cost")
    void spectacleRequiresFiveMana() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RafterDemon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rafter Demon");
        assertThat(gd.stack).isEmpty();
    }
}
