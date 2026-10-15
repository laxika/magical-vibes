package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaveOff.class, GrizzlyBears.class, SuntailHawk.class, Shock.class, Pacifism.class})
class StaveOffTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "BLUE");

        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Only the target gains protection, not the caster's other creatures")
    void protectionLandsOnlyOnTheTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.GREEN);
        assertThat(bystander.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "RED");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLACK", "GREEN"})
    @DisplayName("The remaining colors can be chosen, including the spell's own color")
    void canChooseRemainingColors(String color) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, color);

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.valueOf(color));
        harness.assertInGraveyard(player1, "Stave Off");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARTIFACT", "COLORLESS"})
    @DisplayName("Artifacts and colorless are not legal color choices")
    void rejectsNonColorChoices(String choice) {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player1, choice))
                .isInstanceOf(RuntimeException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Protection makes a pending spell of the chosen color lose its target")
    void savesCreatureFromPendingShock() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("An absent target causes the spell to fail without choosing a color")
    void noColorChoiceWhenTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Stave Off");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Choosing white removes an attached white Aura")
    void protectionRemovesPacifism() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new StaveOff()));
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Pacifism");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "WHITE");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Pacifism");
        harness.assertInGraveyard(player2, "Pacifism");
    }
}
