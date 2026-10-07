package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DutifulKnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellCrumple.class, GrizzlyBears.class, DarkRitual.class,
        ScavengingOoze.class, Scragnoth.class, DutifulKnowledgeSeeker.class})
class SpellCrumpleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell onto the bottom of its owner's library and puts Spell Crumple on the bottom of its owner's library")
    void countersTargetAndPutsBothCardsOnBottomOfOwnersLibraries() {
        GrizzlyBears bears = new GrizzlyBears();
        DarkRitual existingTopCard = new DarkRitual();
        harness.setLibrary(player1, List.of(existingTopCard));
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        SpellCrumple spellCrumple = new SpellCrumple();
        harness.setHand(player2, List.of(spellCrumple));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTopCard, bears);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spellCrumple);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Spell Crumple");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsItselfOnBottomEvenWhenTargetCannotBeCountered() {
        Scragnoth target = new Scragnoth();
        ScavengingOoze existingTopCard = new ScavengingOoze();
        SpellCrumple spellCrumple = new SpellCrumple();
        harness.setHand(player1, List.of(target));
        harness.setHand(player2, List.of(spellCrumple));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(existingTopCard));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTopCard, spellCrumple);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Spell Crumple");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scragnoth");
    }

    @Test
    void goesToGraveyardWhenItsOnlyTargetLeavesTheStack() {
        ScavengingOoze target = new ScavengingOoze();
        SpellCrumple firstCounter = new SpellCrumple();
        SpellCrumple secondCounter = new SpellCrumple();
        harness.setHand(player1, List.of(target, secondCounter));
        harness.setHand(player2, List.of(firstCounter));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, secondCounter);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firstCounter);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Scavenging Ooze");
    }

    @Test
    void canCounterOwnSpellAndPutsItselfBelowThatSpell() {
        ScavengingOoze existingTopCard = new ScavengingOoze();
        ScavengingOoze target = new ScavengingOoze();
        SpellCrumple spellCrumple = new SpellCrumple();
        harness.setLibrary(player1, List.of(existingTopCard));
        harness.setHand(player1, List.of(target, spellCrumple));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingTopCard, target, spellCrumple);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Scavenging Ooze");
    }

    @Test
    void triggersForBothTheCounteredSpellAndItsOwnLibraryMove() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        ScavengingOoze target = new ScavengingOoze();
        SpellCrumple spellCrumple = new SpellCrumple();
        harness.setHand(player1, List.of(target));
        harness.setHand(player2, List.of(spellCrumple));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spellCrumple);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
