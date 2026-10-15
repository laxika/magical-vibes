package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EaglesRescue.class, GrizzlyBears.class, Ornithopter.class, LlanowarElves.class, GiantGrowth.class, Unsummon.class})
class EaglesRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Eagle's Rescue attaches it and grants +2/+2 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EaglesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eagle's Rescue").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability returns Eagle's Rescue attached to a creature with power 1 or less")
    void graveyardAbilityReturnsAttachedToSmallCreature() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateGraveyardAbility(player1, 0, ornithopter.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Eagle's Rescue");
        assertThat(findPermanent(player1, "Eagle's Rescue").getAttachedTo()).isEqualTo(ornithopter.getId());
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Graveyard ability rejects a creature with power greater than 1")
    void graveyardAbilityRejectsLargeCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control with power 1 or less");
    }

    @Test
    @DisplayName("Graveyard ability rejects an opponent's creature")
    void graveyardAbilityRejectsOpponentCreature() {
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control with power 1 or less");
    }

    @Test
    @DisplayName("Graveyard ability can only be activated as a sorcery")
    void graveyardAbilityIsSorcerySpeedOnly() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Power exactly one is legal and blue mana pays the graveyard ability")
    void graveyardAbilityAcceptsPowerOneAndBlueMana() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateGraveyardAbility(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Eagle's Rescue");
        assertThat(findPermanent(player1, "Eagle's Rescue").getAttachedTo()).isEqualTo(elves.getId());
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Counters count toward the graveyard ability's power limit")
    void graveyardAbilityRejectsCreatureBoostedByCounter() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control with power 1 or less");
        harness.assertInGraveyard(player1, "Eagle's Rescue");
    }

    @Test
    @DisplayName("Growing the target in response prevents the graveyard return")
    void graveyardAbilityDoesNotResolveWhenTargetGrows() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0, elves.getId());
        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eagle's Rescue");
        harness.assertNotOnBattlefield(player1, "Eagle's Rescue");
        assertThat(gqs.hasKeyword(gd, elves, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated during combat")
    void graveyardAbilityRejectsCombatTiming() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated while a spell is on the stack")
    void graveyardAbilityRejectsNonemptyStack() {
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.setHand(player1, List.of(new EaglesRescue()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castEnchantment(player1, 0, ornithopter.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, ornithopter.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Casting the Aura can enchant an opponent's creature with power above one")
    void auraSpellCanEnchantLargeOpponentCreatureWithMixedMana() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EaglesRescue()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eagle's Rescue").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removing the target in response leaves Eagle's Rescue in the graveyard")
    void graveyardAbilityDoesNotResolveWhenTargetLeaves() {
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new EaglesRescue()));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0, elves.getId());
        harness.castAndResolveInstant(player1, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Eagle's Rescue");
        harness.assertNotOnBattlefield(player1, "Eagle's Rescue");
    }

    @Test
    @DisplayName("Eagle's Rescue can return again after its enchanted creature leaves")
    void auraCanReturnAgainAfterLosingEnchantedCreature() {
        Permanent firstElves = addCreatureReady(player1, new LlanowarElves());
        Permanent secondElves = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new EaglesRescue(), new Unsummon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, firstElves.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, secondElves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, secondElves, Keyword.FLYING)).isFalse();
        harness.castAndResolveInstant(player1, 0, firstElves.getId());
        harness.assertInGraveyard(player1, "Eagle's Rescue");
        harness.assertNotOnBattlefield(player1, "Eagle's Rescue");

        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        int auraIndex = graveyard.indexOf(graveyard.stream()
                .filter(card -> card instanceof EaglesRescue).findFirst().orElseThrow());
        harness.activateGraveyardAbility(player1, auraIndex, secondElves.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Eagle's Rescue");
        assertThat(findPermanent(player1, "Eagle's Rescue").getAttachedTo()).isEqualTo(secondElves.getId());
        assertThat(gqs.getEffectivePower(gd, secondElves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondElves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, secondElves, Keyword.FLYING)).isTrue();
    }
}
