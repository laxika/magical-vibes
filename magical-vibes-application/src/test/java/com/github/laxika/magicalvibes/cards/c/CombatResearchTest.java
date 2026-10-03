package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombatResearch.class, GrizzlyBears.class, IsamaruHoundOfKonda.class, Shock.class})
class CombatResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary enchanted creature gets +1/+1")
    void legendaryCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature draws after dealing combat damage to a player")
    void enchantedCreatureDrawsOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Legendary enchanted creature has ward {1}")
    void legendaryCreatureHasWard() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);
        castOpponentShock(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Nonlegendary enchanted creature does not have the legendary bonuses")
    void nonlegendaryCreatureDoesNotGetLegendaryBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        castOpponentShock(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private void attachResearch(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CombatResearch());
        aura.setAttachedTo(creature.getId());
    }

    @Test
    void auraResolvesAttachedToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new CombatResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Combat Research").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void enchantedOpponentCreatureDrawsForItsController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachResearch(creature);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void eachAttachedResearchGrantsItsOwnDrawTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);
        attachResearch(creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castOpponentShock(creature);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void ownSpellDoesNotTriggerGrantedWard() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        attachResearch(creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void blockedCreatureDoesNotDrawForDamageToBlocker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachResearch(creature);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, Map.of(0, 0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void wardUsesCreatureControllerRatherThanAuraController() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        attachResearch(creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
