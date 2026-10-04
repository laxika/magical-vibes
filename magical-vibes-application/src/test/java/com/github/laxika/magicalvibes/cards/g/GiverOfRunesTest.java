package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArcumsAstrolabe;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiverOfRunes.class, GrizzlyBears.class})
class GiverOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants protection from a chosen color to another creature you control")
    void grantsProtectionFromChosenColor() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(giver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can grant protection from colorless")
    void grantsProtectionFromColorless() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, null)).isTrue();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Giver of Runes itself")
    void cannotTargetItself() {
        addCreatureReady(player1, new GiverOfRunes());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player1, "Giver of Runes")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

@CardUsed({GiverOfRunes.class, ArcumsAstrolabe.class, LavaDart.class, UniversalAutomaton.class})
class Mh1GiverOfRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Giver of Runes grants another creature protection from a chosen color")
    void grantsProtectionFromChosenColor() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(giver.isTapped()).isTrue();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Can grant protection from colorless")
    void grantsProtectionFromColorless() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(giver.isTapped()).isTrue();
        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target itself, an opponent's creature, or a noncreature")
    void requiresAnotherCreatureYouControl() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent opponentCreature = addCreatureReady(player2, new GiverOfRunes());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArcumsAstrolabe());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("A summoning-sick Giver cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Giver cannot pay its tap cost again")
    void cannotActivateWhileTapped() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        giver.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after Giver leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent giver = addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, giver));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("A target that changes to the opponent's control receives no protection")
    void targetMustStillBeControlledOnResolution() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(target.isProtectionFromColorlessUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("An absent target makes the ability fail to resolve without a color choice")
    void removedTargetGetsNoProtectionChoice() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Colorless protection wears off at the turn boundary")
    void colorlessProtectionWearsOff() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");
        assertThat(gqs.hasProtectionFrom(gd, target, null)).isTrue();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasProtectionFrom(gd, target, null)).isFalse();
    }

    @Test
    @DisplayName("Protection from red makes a pending Lava Dart target illegal")
    void protectionStopsPendingRedSpell() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.setHand(player2, List.of(new LavaDart()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Colorless protection prevents colorless blockers but allows white blockers")
    void colorlessProtectionRestrictsOnlyColorlessBlockers() {
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        Permanent colorlessBlocker = addCreatureReady(player2, new UniversalAutomaton());
        Permanent whiteBlocker = addCreatureReady(player2, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(bls.canBlockAttacker(gd, colorlessBlocker, target,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, whiteBlocker, target,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Protection from white makes another pending Giver ability illegal")
    void whiteProtectionStopsPendingFriendlyAbility() {
        addCreatureReady(player1, new GiverOfRunes());
        addCreatureReady(player1, new GiverOfRunes());
        Permanent target = addCreatureReady(player1, new GiverOfRunes());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.WHITE);
    }
}
