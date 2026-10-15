package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrogButler;
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

@CardUsed({SewerVeillanceCam.class, Forest.class, FrogButler.class})
class SewerVeillanceCamTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may tap or untap a target creature")
    void etbTogglesTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());

        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB only allows creatures as targets")
    void etbOnlyTargetsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(forest.getId());
    }

    @Test
    @DisplayName("Sacrificing it draws two cards and its leave trigger may untap a creature")
    void sacrificeDrawsAndTriggersOnLeave() {
        Permanent cam = harness.addToBattlefieldAndReturn(player1, new SewerVeillanceCam());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        creature.tap();
        harness.setLibrary(player1, List.of(new Forest(), new FrogButler()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cam);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cam.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB may untap a creature you control")
    void etbUntapsOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FrogButler());
        creature.tap();

        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB may be declined after choosing its target")
    void etbMayBeDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());

        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the leave trigger still draws two cards")
    void decliningLeaveTriggerStillDraws() {
        Permanent cam = harness.addToBattlefieldAndReturn(player1, new SewerVeillanceCam());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cam);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cam.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("With no creatures, entry completes and sacrifice still draws")
    void noCreatureTargetsDoesNotPreventDrawing() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sewer-veillance Cam");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sewer-veillance Cam");
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn")
    void canCastOnOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SewerVeillanceCam(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sewer-veillance Cam");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Cam can be sacrificed and its leave trigger may tap a creature")
    void tappedCamCanBeSacrificedAndTapCreature() {
        Permanent cam = harness.addToBattlefieldAndReturn(player1, new SewerVeillanceCam());
        cam.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cam.getCard());
    }
}
