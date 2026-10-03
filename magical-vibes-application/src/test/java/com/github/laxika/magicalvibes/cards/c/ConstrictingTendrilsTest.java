package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.Kaleidostone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConstrictingTendrils.class, CylianSunsinger.class, Kaleidostone.class})
class ConstrictingTendrilsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Constricting Tendrils gives -3/-0 to target creature")
    void resolvesAndWeakensTarget() {
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Cylian Sunsinger");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Cylian Sunsinger");
        assertThat(bear.getPowerModifier()).isEqualTo(-3);
        assertThat(bear.getEffectivePower()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost from Constricting Tendrils wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearId = harness.getPermanentId(player1, "Cylian Sunsinger");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Cylian Sunsinger");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Constricting Tendrils")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.addToBattlefield(player1, new Kaleidostone());
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Kaleidostone");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.setLibrary(player1, List.of(new CylianSunsinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertNotInHand(player1, "Constricting Tendrils");
        harness.assertInGraveyard(player1, "Constricting Tendrils");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Constricting Tendrils");
        harness.assertInHand(player1, "Cylian Sunsinger");
    }

    @Test
    @DisplayName("Constricting Tendrils can weaken an opponent's creature")
    void weakensOpponentsCreature() {
        harness.addToBattlefield(player2, new CylianSunsinger());
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent creature = findPermanent(player2, "Cylian Sunsinger");
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(-1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Cylian Sunsinger");
        harness.assertInGraveyard(player1, "Constricting Tendrils");
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.setLibrary(player1, List.of(new CylianSunsinger()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Constricting Tendrils");
        harness.assertNotInGraveyard(player1, "Constricting Tendrils");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cycling can use colored mana and needs no creature target")
    void cyclingAcceptsColoredManaWithoutCreature() {
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.setLibrary(player1, List.of(new CylianSunsinger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Constricting Tendrils");
        harness.assertInHand(player1, "Cylian Sunsinger");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
