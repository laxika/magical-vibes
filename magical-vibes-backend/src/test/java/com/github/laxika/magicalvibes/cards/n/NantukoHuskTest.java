package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NantukoHuskTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Nantuko Husk has correct card properties")
    void hasCorrectProperties() {
        NantukoHusk card = new NantukoHusk();

        assertThat(card.getName()).isEqualTo("Nantuko Husk");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.INSECT);
    }

    @Test
    @DisplayName("Nantuko Husk has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        NantukoHusk card = new NantukoHusk();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(BoostSelfEffect.class);

        BoostSelfEffect boost = (BoostSelfEffect) ability.getEffects().get(1);
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
    }

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

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nantuko Husk"));
    }

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts boost on the stack")
    void activatingAbilitySacrificesCreatureAndPutsBoostOnStack() {
        Permanent huskPerm = addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Nantuko Husk should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nantuko Husk"));

        // Ability should be on the stack referencing the Husk (non-targeting per MTG rules)
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Nantuko Husk");
        assertThat(entry.getTargetPermanentId()).isEqualTo(huskPerm.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability gives Nantuko Husk +2/+2")
    void resolvingAbilityBoostsHusk() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
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
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, createTokenCreature("Saproling Token"));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        UUID tokenId = harness.getPermanentId(player1, "Saproling Token");
        harness.activateAbility(player1, 0, null, tokenId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent husk = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(husk.getCard().getName()).isEqualTo("Nantuko Husk");
        assertThat(husk.getPowerModifier()).isEqualTo(4);
        assertThat(husk.getToughnessModifier()).isEqualTo(4);
        assertThat(husk.getEffectivePower()).isEqualTo(6);
        assertThat(husk.getEffectiveToughness()).isEqualTo(6);

        // Both sacrificed creatures should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saproling Token"));
    }

    @Test
    @DisplayName("Can sacrifice Nantuko Husk to its own ability")
    void canSacrificeItself() {
        addNantukoHuskReady(player1);
        UUID huskId = harness.getPermanentId(player1, "Nantuko Husk");

        harness.activateAbility(player1, 0, null, huskId);

        GameData gd = harness.getGameData();

        // Husk should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nantuko Husk"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nantuko Husk"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nantuko Husk");
    }

    @Test
    @DisplayName("Boost fizzles when Husk sacrifices itself")
    void boostFizzlesWhenHuskSacrificesItself() {
        addNantukoHuskReady(player1);
        UUID huskId = harness.getPermanentId(player1, "Nantuko Husk");

        harness.activateAbility(player1, 0, null, huskId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Husk is in the graveyard, ability fizzled — no crash
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nantuko Husk"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nantuko Husk"));
    }

    // ===== No mana cost =====

    @Test
    @DisplayName("Ability has no mana cost — can activate without mana")
    void canActivateWithoutMana() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        // No mana added — should still work
        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Ability does not tap Nantuko Husk")
    void activatingAbilityDoesNotTap() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        Permanent husk = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(husk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when Husk is tapped")
    void canActivateWhenTapped() {
        Permanent huskPerm = addNantukoHuskReady(player1);
        huskPerm.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness since it does not require tap")
    void canActivateWithSummoningSickness() {
        NantukoHusk card = new NantukoHusk();
        Permanent huskPerm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(huskPerm);

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    // ===== Boost resets at end of turn =====

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
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
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

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
    @DisplayName("Cannot activate ability without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addNantukoHuskReady(player1);
        // No other creatures on the battlefield, trying to pass null target

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentCreature() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature you control");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-creature permanent")
    void cannotSacrificeNonCreature() {
        addNantukoHuskReady(player1);
        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setManaCost("{1}{W}");
        enchantment.setColor(CardColor.WHITE);
        harness.addToBattlefield(player1, enchantment);
        UUID enchantmentId = harness.getPermanentId(player1, "Test Enchantment");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature");
    }

    // ===== Logging =====

    @Test
    @DisplayName("Sacrificing a creature logs the sacrifice")
    void sacrificingCreatureLogsIt() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("sacrifices Grizzly Bears"));
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Nantuko Husk's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the boost")
    void resolvingAbilityLogsBoost() {
        addNantukoHuskReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("gets +2/+2"));
    }

    // ===== Helper methods =====

    private Permanent addNantukoHuskReady(Player player) {
        NantukoHusk card = new NantukoHusk();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}

