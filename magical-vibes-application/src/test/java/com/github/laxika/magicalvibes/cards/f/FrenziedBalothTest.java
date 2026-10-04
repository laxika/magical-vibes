package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedBaloth.class, Cancel.class, GrizzlyBears.class, Shock.class, TurnToFrog.class})
class FrenziedBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Frenzied Baloth can't be countered")
    void cannotBeCountered() {
        FrenziedBaloth baloth = new FrenziedBaloth();
        harness.setHand(player1, List.of(baloth));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, baloth.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frenzied Baloth");
        harness.assertNotInGraveyard(player1, "Frenzied Baloth");
    }

    @Test
    @DisplayName("Frenzied Baloth makes your creature spells unable to be countered")
    void protectsCreatureSpellsYouControl() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Frenzied Baloth prevents combat damage prevention from an opposing creature")
    void combatDamageCannotBePrevented() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        gd.playerDamagePreventionShields.put(player1.getId(), 5);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDamagePreventionShields).containsEntry(player1.getId(), 5);
    }

    @Test
    @DisplayName("Frenzied Baloth does not prevent noncombat damage prevention")
    void noncombatDamageCanBePrevented() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        gd.playerDamagePreventionShields.put(player1.getId(), 2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities removes protection from counterspells")
    void losingAbilitiesRemovesCreatureSpellProtection() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Frenzied Baloth"));

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opposing Baloth does not protect your creature spells")
    void doesNotProtectOpponentsCreatureSpells() {
        harness.addToBattlefield(player2, new FrenziedBaloth());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Baloth does not protect its controller's noncreature spells")
    void doesNotProtectNoncreatureSpells() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("A newly cast Baloth can attack and its combat damage cannot be prevented")
    void hasteAndUnpreventableCombatDamage() {
        harness.setHand(player1, List.of(new FrenziedBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        gd.playerDamagePreventionShields.put(player2.getId(), 5);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 5);
    }

    @Test
    @DisplayName("Baloth tramples over a blocker while combat prevention shields remain unused")
    void tramplesOverBlocker() {
        addCreatureReady(player1, new FrenziedBaloth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gd.playerDamagePreventionShields.put(player2.getId(), 5);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Frenzied Baloth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 5);
    }
}
