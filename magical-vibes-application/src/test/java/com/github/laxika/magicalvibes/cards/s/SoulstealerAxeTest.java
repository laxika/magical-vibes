package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulstealerAxe.class, HillGiant.class, GrayOgre.class, Forest.class, GrizzlyBears.class})
class SoulstealerAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has trample and seeks a card matching its combat damage")
    void equippedCreatureTramplesAndSeeksByCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrayOgre()));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Gray Ogre");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip {2} attaches Soulstealer Axe to a creature you control")
    void equipAttachesToCreature() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void seeksOnlyOnceAndPreservesOrderOfOtherLibraryCards() {
        harness.setHand(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setLibrary(player1, List.of(forest, new GrayOgre(), bears, new GrayOgre(), giant));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Gray Ogre");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> !(card instanceof GrayOgre)).toList()).containsExactly(forest, bears, giant);
    }

    @Test
    void noMatchingManaValueLeavesLibraryUnchanged() {
        harness.setHand(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        harness.setLibrary(player1, List.of(forest, bears, giant));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears, giant);
    }

    @Test
    void axeControllerSeeksWhenOpponentsEquippedCreatureDealsDamage() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        harness.setLibrary(player1, List.of(new GrayOgre()));
        GrayOgre opponentsCard = new GrayOgre();
        harness.setLibrary(player2, List.of(opponentsCard));

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertInHand(player1, "Gray Ogre");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void trampleSeeksByDamageToPlayerRatherThanTotalDamage() {
        harness.setHand(player1, List.of());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new SoulstealerAxe());
        axe.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new SoulstealerAxe(), new GrayOgre()));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertInHand(player1, "Soulstealer Axe");
        harness.assertNotInHand(player1, "Gray Ogre");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
