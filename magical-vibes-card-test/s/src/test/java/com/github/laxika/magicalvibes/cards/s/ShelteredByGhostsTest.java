package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShelteredByGhosts.class, GrizzlyBears.class, FountainOfYouth.class,
        Naturalize.class, Shock.class, ProdigalPyromancer.class, Forest.class})
class ShelteredByGhostsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nonland permanent and boosts the enchanted creature")
    void exilesPermanentAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castAndResolve(creature.getId(), target.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Sheltered by Ghosts leaves")
    void exiledPermanentReturnsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID auraId = harness.getPermanentId(player1, "Sheltered by Ghosts");

        harness.castAndResolveInstant(player2, 0, auraId);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Fountain of Youth"));
    }

    @Test
    @DisplayName("Enchanted creature has ward {2}")
    void enchantedCreatureHasWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot exile a permanent controlled by the Aura's controller")
    void cannotTargetOwnPermanentForExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShelteredByGhosts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(creature.getId(), ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    @Test
    void resolvesWithoutAnOpposingNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareAura();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sheltered by Ghosts");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesExileTargetAfterAuraResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareAura();
        harness.castEnchantment(player1, 0, creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sheltered by Ghosts");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void doesNotExileIfAuraLeavesBeforeTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareAura();
        harness.castEnchantment(player1, 0, List.of(creature.getId(), target.getId()));
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Sheltered by Ghosts"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sheltered by Ghosts");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolveAndReturnsExiledCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sheltered by Ghosts");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sheltered by Ghosts");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());
        addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void combatDamageGainsLifeFromLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castAndResolve(creature.getId(), target.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        creature.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareAura();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Sheltered by Ghosts");
    }

    @Test
    void cannotExileAnOpponentsLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepareAura();

        assertThatThrownBy(() -> harness.castEnchantment(
                player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    @Test
    void auraDoesNotEnterIfItsCreatureDiesInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareAura();
        harness.castEnchantment(player1, 0, List.of(creature.getId(), target.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sheltered by Ghosts");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void prepareAura() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShelteredByGhosts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve(UUID creatureId, UUID permanentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShelteredByGhosts()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, List.of(creatureId, permanentId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
