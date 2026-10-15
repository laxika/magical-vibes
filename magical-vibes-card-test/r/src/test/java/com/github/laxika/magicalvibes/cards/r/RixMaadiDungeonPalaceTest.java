package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RixMaadiDungeonPalace.class, GhostQuarter.class, RakdosCarnarium.class})
class RixMaadiDungeonPalaceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tapForColorlessMana() {
        Permanent palace = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(palace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each player chooses a card to discard in turn order")
    void eachPlayerDiscards() {
        Permanent palace = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        harness.setHand(player1, List.of(new GhostQuarter()));
        harness.setHand(player2, List.of(new RakdosCarnarium()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(palace.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Rakdos Carnarium");
    }

    @Test
    @DisplayName("Each player with an empty hand skips the discard choice")
    void emptyHandsDoNotCreateDiscardChoices() {
        Permanent palace = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(palace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The discard ability can be activated only as a sorcery")
    void discardAbilityIsSorcerySpeed() {
        harness.addToBattlefield(player1, new RixMaadiDungeonPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardsHappenTogetherAfterBothPlayersChoose() {
        harness.addToBattlefield(player1, new RixMaadiDungeonPalace());
        harness.setHand(player1, List.of(new GhostQuarter(), new RakdosCarnarium()));
        harness.setHand(player2, List.of(new GhostQuarter(), new RakdosCarnarium()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertNotInGraveyard(player1, "Rakdos Carnarium");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Rakdos Carnarium");
        harness.assertInGraveyard(player2, "Ghost Quarter");
        harness.assertInHand(player1, "Ghost Quarter");
        harness.assertInHand(player2, "Rakdos Carnarium");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyControllerHandDoesNotPreventOpponentDiscard() {
        harness.addToBattlefield(player1, new RixMaadiDungeonPalace());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GhostQuarter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Ghost Quarter");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardAbilityCannotBeActivatedOutsideMainPhase() {
        Permanent palace = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(palace.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardAbilityCannotBeActivatedWithNonemptyStack() {
        harness.addToBattlefield(player1, new RixMaadiDungeonPalace());
        Permanent secondPalace = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondPalace.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }
}
