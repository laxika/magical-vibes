package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UntamedMight.class, CarapaceForger.class, AccordersShield.class})
class UntamedMightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Untamed Might puts it on the stack with target and X value")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 3, bearId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost")
    void cannotCastWithoutBaseMana() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay X with any color mana")
    void canPayXWithAnyColor() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 3, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving gives target creature +X/+X")
    void resolvesAndBoostsTarget() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 5, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(5);
        assertThat(bear.getToughnessModifier()).isEqualTo(5);
        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("X=0 gives +0/+0")
    void xZeroGivesNoBoost() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearId = harness.getPermanentId(player2, "Carapace Forger");
        harness.castInstant(player1, 0, 2, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 3, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 3, bearId);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Accorder's Shield");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID bearId = harness.getPermanentId(player1, "Carapace Forger");
        harness.castInstant(player1, 0, 3, bearId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Untamed Might");
    }

    @Test
    @DisplayName("Each copy uses its own X value and boosts only its target")
    void separateSpellsKeepTheirOwnXValues() {
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addToBattlefield(player2, new CarapaceForger());
        harness.setHand(player1, List.of(new UntamedMight(), new UntamedMight()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        UUID firstTarget = harness.getPermanentId(player1, "Carapace Forger");
        UUID secondTarget = harness.getPermanentId(player2, "Carapace Forger");
        harness.castInstant(player1, 0, 2, firstTarget);
        harness.castInstant(player1, 0, 5, secondTarget);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Carapace Forger").getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Carapace Forger").getToughnessModifier()).isZero();
        assertThat(findPermanent(player2, "Carapace Forger").getPowerModifier()).isEqualTo(5);
        assertThat(findPermanent(player2, "Carapace Forger").getToughnessModifier()).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Carapace Forger").getPowerModifier()).isEqualTo(2);
        assertThat(findPermanent(player1, "Carapace Forger").getToughnessModifier()).isEqualTo(2);
        assertThat(findPermanent(player2, "Carapace Forger").getPowerModifier()).isEqualTo(5);
        assertThat(findPermanent(player2, "Carapace Forger").getToughnessModifier()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
