package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapturedByTheConsulate.class, GrizzlyBears.class, Boomerang.class,
        PeelFromReality.class, LavaAxe.class, AuraGraft.class, HolyStrength.class,
        ChandrasPyrohelix.class})
class CapturedByTheConsulateTest extends BaseCardTest {

    @Test
    @DisplayName("Captured by the Consulate can enchant only an opponent's creature")
    void canEnchantOnlyOpponentsCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CapturedByTheConsulate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A single-target opponent spell is redirected to the enchanted creature")
    void redirectsSingleTargetSpell() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("A spell with multiple targets is not redirected")
    void doesNotRedirectMultipleTargetSpell() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player1, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        PeelFromReality peel = new PeelFromReality();
        harness.setHand(player2, List.of(peel));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, List.of(otherCreature.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(otherCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("A spell that cannot target the enchanted creature keeps its target")
    void keepsTargetWhenEnchantedCreatureIsIllegal() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature can still block")
    void enchantedCreatureCanBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Aura controller's spells are not redirected")
    void doesNotRedirectControllersSpell() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, originalTarget.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("An opponent's Aura spell is redirected")
    void redirectsAuraSpell() {
        Permanent originalTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HolyStrength()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, originalTarget.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Holy Strength").getAttachedTo())
                .isEqualTo(enchantedCreature.getId());
    }

    @Test
    @DisplayName("Redirection uses the creature enchanted when the trigger resolves")
    void redirectsToCurrentEnchantedCreatureAfterAuraMoves() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent formerlyEnchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent currentlyEnchanted = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(formerlyEnchanted);
        Permanent aura = findPermanent(player1, "Captured by the Consulate");
        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, originalTarget.getId());

        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, currentlyEnchanted.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(currentlyEnchanted.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(formerlyEnchanted.getId()))
                .noneMatch(p -> p.getId().equals(currentlyEnchanted.getId()));
    }

    @Test
    @DisplayName("A divided damage spell with one target redirects its damage")
    void redirectsSingleTargetDividedDamageSpell() {
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castAuraOn(enchantedCreature);
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, Map.of(originalTarget.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(originalTarget.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new CapturedByTheConsulate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
