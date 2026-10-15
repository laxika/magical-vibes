package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BranchblightStalker;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HexgoldSlash;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PrologueToPhyresis;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({MeliraTheLivingCure.class, GoForTheThroat.class, GrizzlyBears.class,
        IchorRats.class, Naturalize.class, Spellbook.class, BranchblightStalker.class,
        HexgoldSlash.class, PrologueToPhyresis.class, DressDown.class})
class MeliraTheLivingCureTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces the first poison event with one counter and stops later poison events that turn")
    void limitsPoisonCountersForTheTurn() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        harness.setHand(player1, List.of(new IchorRats(), new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a targeted creature when it is put into a graveyard this turn")
    void returnsTargetedCreature() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a targeted artifact when it is put into a graveyard this turn")
    void returnsTargetedArtifact() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.activateAbility(player1, 0, null, spellbook.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, spellbook.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Cannot target Melira itself")
    void cannotTargetSelf() {
        Permanent melira = harness.addToBattlefieldAndReturn(player1, new MeliraTheLivingCure());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, melira.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or artifact");
        harness.assertOnBattlefield(player1, "Melira, the Living Cure");
    }

    @Test
    @DisplayName("Poison restriction lasts after Melira is exiled to activate its ability")
    void poisonRestrictionPersistsAfterMeliraLeaves() {
        MeliraTheLivingCure melira = new MeliraTheLivingCure();
        harness.addToBattlefield(player1, melira);
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        harness.setLibrary(player2, List.of(new BranchblightStalker(), new BranchblightStalker()));
        harness.setHand(player2, List.of(new PrologueToPhyresis(), new PrologueToPhyresis()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, stalker.getId());
        harness.assertNotOnBattlefield(player1, "Melira, the Living Cure");
        assertThat(gd.findExiledCard(melira.getId())).isNotNull();
        assertThat(gd.findExiledCard(melira.getId()).card()).isSameAs(melira);
        resolveAllTriggers();

        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Delayed return retains Melira as source and its ability controller as controller")
    void delayedReturnRetainsSourceAndController() {
        MeliraTheLivingCure melira = new MeliraTheLivingCure();
        harness.addToBattlefield(player1, melira);
        Permanent stalker = harness.addToBattlefieldAndReturn(player2, new BranchblightStalker());
        harness.activateAbility(player1, 0, null, stalker.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, stalker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(melira.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Branchblight Stalker");
        harness.assertNotOnBattlefield(player1, "Branchblight Stalker");
        harness.assertNotInGraveyard(player2, "Branchblight Stalker");
    }

    @Test
    @DisplayName("A target dying before Melira's ability resolves does not return")
    void targetDiesBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        Permanent stalker = harness.addToBattlefieldAndReturn(player2, new BranchblightStalker());
        harness.activateAbility(player1, 0, null, stalker.getId());

        harness.setHand(player1, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, stalker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Branchblight Stalker");
        harness.assertNotOnBattlefield(player2, "Branchblight Stalker");
        harness.assertNotOnBattlefield(player1, "Melira, the Living Cure");
    }

    @Test
    @DisplayName("Melira reduces a toxic 2 event to one poison counter without preventing damage")
    void reducesMultiplePoisonCountersToOne() {
        harness.addToBattlefield(player2, new MeliraTheLivingCure());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BranchblightStalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Melira does not replace poison events while it has lost its abilities")
    void abilityRemovalDisablesPoisonReplacement() {
        harness.addToBattlefield(player1, new MeliraTheLivingCure());
        harness.setLibrary(player2, List.of(new BranchblightStalker(),
                new BranchblightStalker(), new BranchblightStalker()));
        harness.setHand(player2, List.of(new DressDown(), new PrologueToPhyresis(),
                new PrologueToPhyresis()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castEnchantment(player2, 0);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Dress Down");

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Melira's poison restriction expires at the end of the turn")
    void poisonRestrictionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new MeliraTheLivingCure());
        harness.setLibrary(player1, List.of(new BranchblightStalker(),
                new BranchblightStalker(), new BranchblightStalker()));
        harness.setLibrary(player2, List.of(new BranchblightStalker()));
        harness.setHand(player1, List.of(new PrologueToPhyresis(), new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrologueToPhyresis()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
