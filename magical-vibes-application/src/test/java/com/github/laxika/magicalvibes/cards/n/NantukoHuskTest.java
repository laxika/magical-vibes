package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoHusk.class, GrizzlyBears.class, Island.class})
class NantukoHuskTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Nantuko Husk puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new NantukoHusk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nantuko Husk");
    }

    @Test
    @DisplayName("Resolving Nantuko Husk puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new NantukoHusk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nantuko Husk");
    }

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent huskPerm = addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Nantuko Husk should still be on the battlefield
        harness.assertOnBattlefield(player1, "Nantuko Husk");

        // Ability should be on the stack referencing the Husk (non-targeting per MTG rules)
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Nantuko Husk");
        assertThat(entry.getTargetId()).isEqualTo(huskPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Nantuko Husk +2/+2")
    void resolvingAbilityBoostsHusk() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent husk = findPermanent(player1, "Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(2);
        assertThat(husk.getToughnessModifier()).isEqualTo(2);
        assertThat(husk.getEffectivePower()).isEqualTo(4);
        assertThat(husk.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID glorySeekerId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tokenId);
        harness.passBothPriorities();

        Permanent husk = findPermanent(player1, "Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(4);
        assertThat(husk.getToughnessModifier()).isEqualTo(4);
        assertThat(husk.getEffectivePower()).isEqualTo(6);
        assertThat(husk.getEffectiveToughness()).isEqualTo(6);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Saproling Token");
    }

    @Test
    @DisplayName("Can activate twice before either ability resolves")
    void canStackMultipleActivations() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        List<Permanent> glorySeekers = findPermanents(player1, "Grizzly Bears");
        assertThat(glorySeekers).hasSize(2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekers.getFirst().getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekers.get(1).getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent husk = findPermanent(player1, "Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(4);
        assertThat(husk.getToughnessModifier()).isEqualTo(4);
        assertThat(husk.getEffectivePower()).isEqualTo(6);
        assertThat(husk.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Can sacrifice Nantuko Husk to its own ability")
    void canSacrificeItself() {
        addCreatureReady(player1, new NantukoHusk());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Husk should be sacrificed
        harness.assertNotOnBattlefield(player1, "Nantuko Husk");
        harness.assertInGraveyard(player1, "Nantuko Husk");

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nantuko Husk");
    }

    @Test
    @DisplayName("Ability resolves with no effect when Husk sacrifices itself")
    void abilityResolvesWithNoEffectWhenHuskSacrificesItself() {
        addCreatureReady(player1, new NantukoHusk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Husk is in the graveyard, and the non-targeting ability resolved with no effect
        harness.assertNotOnBattlefield(player1, "Nantuko Husk");
        harness.assertInGraveyard(player1, "Nantuko Husk");
    }

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        // No mana added — should still work
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Nantuko Husk")
    void activatingAbilityDoesNotTap() {
        Permanent huskPerm = addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(huskPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when Husk is tapped")
    void canActivateWhenTapped() {
        Permanent huskPerm = addCreatureReady(player1, new NantukoHusk());
        huskPerm.tap();
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness since it does not require tap")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new NantukoHusk());

        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        Permanent husk = findPermanent(player1, "Nantuko Husk");
        assertThat(husk.getEffectivePower()).isEqualTo(4);
        assertThat(husk.getEffectiveToughness()).isEqualTo(4);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(husk.getPowerModifier()).isEqualTo(0);
        assertThat(husk.getToughnessModifier()).isEqualTo(0);
        assertThat(husk.getEffectivePower()).isEqualTo(2);
        assertThat(husk.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability resolves but has no effect if Husk is removed before resolution")
    void abilityResolvesButNoEffectIfHuskRemoved() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        // Remove Husk before resolution (e.g., killed by an instant)
        harness.getGameData().playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Nantuko Husk"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Ability resolved (doesn't fizzle since it's non-targeting) but had no effect
    }

    @Test
    @DisplayName("When Husk is the only creature, it auto-sacrifices itself")
    void autoSacrificesItselfWhenOnlyCreature() {
        addCreatureReady(player1, new NantukoHusk());

        // Husk is the only creature — auto-pay sacrifices itself
        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Nantuko Husk");
        harness.assertInGraveyard(player1, "Nantuko Husk");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-creature permanents are not eligible for sacrifice — auto-pays with Husk")
    void nonCreaturePermanentNotEligibleForSacrifice() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new Island());

        // Only one creature (Husk) — auto-pay sacrifices Husk, not the land
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Nantuko Husk");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Nantuko Husk");
    }

    @Test
    @DisplayName("Sacrifice cost only considers creatures controlled by Nantuko Husk's controller")
    void sacrificeCostCannotUseOpponentCreature() {
        addCreatureReady(player1, new NantukoHusk());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ownCreature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Nantuko Husk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a creature logs the sacrifice")
    void sacrificingCreatureLogsIt() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(gameLogContains("sacrifices Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(gameLogContains("activates Nantuko Husk's ability")).isTrue();
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addCreatureReady(player1, new NantukoHusk());
        UUID glorySeekerId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        assertThat(gameLogContains("gets +2/+2")).isTrue();
    }

    @Test
    @DisplayName("An activation boosts only the Husk that activated it, after resolution")
    void boostsOnlyItsSourceAfterResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NantukoHusk());
        Permanent otherHusk = harness.addToBattlefieldAndReturn(player1, new NantukoHusk());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(source.getPowerModifier()).isZero();
        assertThat(source.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(2);
        assertThat(source.getToughnessModifier()).isEqualTo(2);
        assertThat(otherHusk.getPowerModifier()).isZero();
        assertThat(otherHusk.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Card createTokenCreature(String name) {
        Card card = new GrizzlyBears();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}

