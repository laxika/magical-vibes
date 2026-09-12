package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.FlameJet;
import com.github.laxika.magicalvibes.cards.m.Magnify;
import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quash.class, FlameJet.class, Magnify.class, MetathranSoldier.class})
class QuashTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant spell and exiles every same-name copy from graveyard, hand, and library")
    void countersInstantAndExilesAllCopies() {
        Magnify castCopy = new Magnify();
        var creature = harness.addToBattlefieldAndReturn(player1, new MetathranSoldier());
        harness.setHand(player1, List.of(castCopy, new Magnify()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setGraveyard(player1, List.of(new Magnify()));
        harness.setLibrary(player1, List.of(new Magnify(), new MetathranSoldier()));

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        // All four Magnifies (cast + hand + graveyard + library) are exiled.
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Magnify"))
                .hasSize(4);

        harness.assertNotInHand(player1, "Magnify");
        harness.assertNotInGraveyard(player1, "Magnify");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Magnify"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Metathran Soldier"));
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        MetathranSoldier soldier = new MetathranSoldier();
        harness.setHand(player1, List.of(soldier));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, soldier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
    }

    @Test
    @DisplayName("Counters a sorcery spell and goes to its caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        FlameJet castCopy = new FlameJet();
        harness.setHand(player1, List.of(castCopy));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(castCopy.getId()));
        harness.assertInGraveyard(player2, "Quash");
    }

    @Test
    @DisplayName("Fizzles without searching when the target spell leaves the stack")
    void fizzlesIfTargetLeavesStack() {
        Magnify castCopy = new Magnify();
        harness.setHand(player1, List.of(castCopy, new Magnify()));
        harness.setGraveyard(player1, List.of(new Magnify()));
        harness.setLibrary(player1, List.of(new Magnify()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Quash()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, castCopy.getId());

        gd.stack.removeIf(stackEntry -> stackEntry.getTargetableId().equals(castCopy.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Magnify"));
        harness.assertInHand(player1, "Magnify");
        harness.assertInGraveyard(player1, "Magnify");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Magnify"));
        harness.assertInGraveyard(player2, "Quash");
    }
}
