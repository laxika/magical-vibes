package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AltarOfTheLost.class, ThinkTwice.class, GrizzlyBears.class})
class AltarOfTheLostTest extends BaseCardTest {

    // ===== Enters tapped =====

    @Test
    @DisplayName("Enters the battlefield tapped when cast")
    void entersTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new AltarOfTheLost()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent altar = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(altar.isTapped()).isTrue();
    }

    // ===== Mana ability: two sequential color choices =====

    @Test
    @DisplayName("Activating ability prompts for two color choices — choosing same color both times")
    void abilityAddsSameColorTwice() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");  // first mana
        harness.handleListChoice(player1, "BLUE");  // second mana

        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability prompts for two color choices — choosing different colors")
    void abilityAddsDifferentColors() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");  // first mana
        harness.handleListChoice(player1, "BLACK");  // second mana

        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLACK)).isEqualTo(1);
    }

    // ===== Flashback spending =====

    @Test
    @DisplayName("Flashback-only mana can pay for flashback spell cost")
    void flashbackManaPayForFlashbackSpell() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        // Activate Altar: choose blue twice, get 2 flashback-only blue
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Think Twice has flashback {2}{U} — needs 1 blue + 2 generic
        // We have 2 flashback-only blue. Add 1 more regular colorless to cover the total.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setGraveyard(player1, List.of(new ThinkTwice()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveFlashback(player1, 0, null);

        // Think Twice draws a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        // Flashback-only mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Mixed-color flashback-only mana can pay for flashback spell")
    void mixedColorFlashbackManaPaysForFlashbackSpell() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        // Activate Altar: choose blue + red
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "RED");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Think Twice has flashback {2}{U} — needs 1 blue + 2 generic
        // We have 1 flashback-only blue + 1 flashback-only red. Add 1 colorless.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setGraveyard(player1, List.of(new ThinkTwice()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Flashback-only mana is not spent when casting a normal spell from hand")
    void flashbackManaNotUsedForNormalSpell() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        // Activate Altar: choose green twice
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Grizzly Bears ({1}{G}) with regular mana only
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears enters using regular green mana
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        // Flashback-only green should be untouched
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast flashback spell with only flashback-only mana when not enough")
    void cannotCastFlashbackWithInsufficientFlashbackMana() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        // Activate Altar: choose red twice
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Think Twice has flashback {2}{U} — needs 1 blue + 2 generic
        // We have 2 flashback-only red — no blue available at all
        harness.setGraveyard(player1, List.of(new ThinkTwice()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback-only mana drains at step/phase transitions")
    void flashbackManaDrainsAtPhaseTransition() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());
        altar.untap();

        // Activate Altar: choose blue twice
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLUE)).isEqualTo(2);

        // Drain non-persistent mana (simulating phase transition)
        gd.playerManaPools.get(player1.getId()).drainNonPersistent();

        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being cast")
    void entersTappedWithoutBeingCast() {
        Permanent altar = harness.enterBattlefieldAndReturn(player1, new AltarOfTheLost());

        assertThat(altar.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Mana ability taps its source and resolves without using the stack")
    void manaAbilityRequiresUntappedSourceAndDoesNotUseStack() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheLost());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyManaTotal()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyManaTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Having flashback does not make a spell cast from hand eligible for the restricted mana")
    void cannotSpendRestrictedManaOnFlashbackCardFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AltarOfTheLost());
        harness.setHand(player1, List.of(new ThinkTwice()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyMana(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana empties when the engine advances out of the main phase")
    void restrictedManaDrainsAtActualPhaseTransition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AltarOfTheLost());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyManaTotal()).isEqualTo(2);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).getFlashbackOnlyManaTotal()).isZero();
    }
}
