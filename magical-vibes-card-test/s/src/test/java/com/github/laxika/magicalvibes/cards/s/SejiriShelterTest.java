package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SejiriShelter.class, SejiriGlacier.class, ExpeditionHealer.class})
class SejiriShelterTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(CardColor.class)
    void grantsProtectionFromChosenColorToTargetCreature(CardColor color) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, color.name());

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(color);
    }

    @Test
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void protectionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.RED);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    void doesNotChooseColorWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sejiri Shelter");
    }

    @Test
    void protectionFromWhitePreventsAnotherShelterFromTargetingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter(), new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "COLORLESS"})
    void cannotChooseNonColorProtection(String choice) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SejiriShelter()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        PendingInteraction.ColorChoice prompt =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(prompt.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        assertThatThrownBy(() -> harness.handleListChoice(player1, choice))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isSameAs(prompt);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();

        harness.handleListChoice(player1, "GREEN");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
    }

    @Test
    void landFaceUsesLandPlayAndCannotBePlayedTwiceInOneTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SejiriShelter(), new SejiriShelter()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void landFaceEntersTappedAndProducesWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SejiriShelter()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(SejiriGlacier.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
    }
}
