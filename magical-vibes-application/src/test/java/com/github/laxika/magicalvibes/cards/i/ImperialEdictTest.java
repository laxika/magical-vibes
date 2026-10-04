package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImperialEdict.class, ForestBear.class, AlertShuInfantry.class, Forest.class})
class ImperialEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent with one creature has it destroyed automatically")
    void opponentWithOneCreatureDestroyedAutomatically() {
        harness.addToBattlefield(player2, new ForestBear());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Opponent with multiple creatures is prompted to choose which to destroy")
    void opponentWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player2, new ForestBear());
        harness.addToBattlefield(player2, new AlertShuInfantry());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DestroyChosenCreature.class);
    }

    @Test
    @DisplayName("Opponent chooses which creature to destroy")
    void opponentChoosesCreatureToDestroy() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.addToBattlefield(player2, new AlertShuInfantry());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handlePermanentChosen(player2, bear.getId());

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Indestructible creature is not destroyed (destroy, not sacrifice)")
    void indestructibleCreatureSurvives() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        bear.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("A creature with a regeneration shield survives")
    void regenerationShieldPreventsDestruction() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        bear.setRegenerationShield(1);

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest Bear");
        harness.assertNotInGraveyard(player2, "Forest Bear");
    }

    @Test
    @DisplayName("A noncreature permanent is not eligible")
    void noncreaturePermanentIsNotEligible() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new ForestBear());

        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
    }

    @Test
    @DisplayName("No effect when opponent has no creatures")
    void noEffectWhenOpponentHasNoCreatures() {
        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("no creatures to destroy")).isTrue();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent may choose an indestructible creature even when another could be destroyed")
    void opponentCanChooseIndestructibleCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        bear.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new AlertShuInfantry());
        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, bear.getId());

        harness.assertOnBattlefield(player2, "Forest Bear");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertNotInGraveyard(player2, "Forest Bear");
        harness.assertInGraveyard(player1, "Imperial Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Shroud and hexproof do not prevent choosing and destroying a creature")
    void chosenCreatureIsNotTargeted() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        bear.getGrantedKeywords().add(Keyword.SHROUD);
        bear.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.addToBattlefield(player2, new AlertShuInfantry());
        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, bear.getId());

        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertInGraveyard(player1, "Imperial Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the opponent can choose, and only among creatures they control")
    void invalidChoicesPreserveOpponentChoice() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.addToBattlefield(player2, new AlertShuInfantry());
        harness.setHand(player1, List.of(new ImperialEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player2, opponentBear.getId());

        harness.assertOnBattlefield(player1, "Forest Bear");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Alert Shu Infantry");
        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertInGraveyard(player1, "Imperial Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
