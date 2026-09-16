package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({NantukoHusk.class, GlorySeeker.class, Swamp.class})
class NantukoHuskTest extends BaseCardTest {

    // ===== Casting =====

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

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent huskPerm = addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();

        // Glory Seeker should be sacrificed
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertInGraveyard(player1, "Glory Seeker");

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
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent husk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(husk.getCard().getName()).isEqualTo("Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(2);
        assertThat(husk.getToughnessModifier()).isEqualTo(2);
        assertThat(husk.getEffectivePower()).isEqualTo(4);
        assertThat(husk.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate multiple times by sacrificing different creatures")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, tokenId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent husk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(husk.getCard().getName()).isEqualTo("Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(4);
        assertThat(husk.getToughnessModifier()).isEqualTo(4);
        assertThat(husk.getEffectivePower()).isEqualTo(6);
        assertThat(husk.getEffectiveToughness()).isEqualTo(6);

        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotInGraveyard(player1, "Saproling Token");
    }

    @Test
    @DisplayName("Can activate twice before either ability resolves")
    void canStackMultipleActivations() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GlorySeeker());

        List<Permanent> glorySeekers = findPermanents(player1, "Glory Seeker");
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
                .filter(card -> card.getName().equals("Glory Seeker")))
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

    // ===== No mana cost =====

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        // No mana added — should still work
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Nantuko Husk")
    void activatingAbilityDoesNotTap() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        Permanent husk = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(husk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when Husk is tapped")
    void canActivateWhenTapped() {
        Permanent huskPerm = addCreatureReady(player1, new NantukoHusk());
        huskPerm.tap();
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness since it does not require tap")
    void canActivateWithSummoningSickness() {
        NantukoHusk card = new NantukoHusk();
        Permanent huskPerm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(huskPerm);

        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        Permanent husk = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
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

    // ===== Ability resolves even if Husk is removed (non-targeting) =====

    @Test
    @DisplayName("Ability resolves but has no effect if Husk is removed before resolution")
    void abilityResolvesButNoEffectIfHuskRemoved() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

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

    // ===== Validation errors =====

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
        harness.addToBattlefield(player1, new Swamp());

        // Only one creature (Husk) — auto-pay sacrifices Husk, not the land
        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Nantuko Husk");
        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Nantuko Husk");
    }

    // ===== Logging =====

    @Test
    @DisplayName("Sacrificing a creature logs the sacrifice")
    void sacrificingCreatureLogsIt() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("sacrifices Glory Seeker"));
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("activates Nantuko Husk's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addCreatureReady(player1, new NantukoHusk());
        harness.addToBattlefield(player1, new GlorySeeker());
        UUID glorySeekerId = harness.getPermanentId(player1, "Glory Seeker");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, glorySeekerId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gets +2/+2"));
    }

    // ===== Helper methods =====

    private Card createTokenCreature(String name) {
        Card card = new Card();
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

