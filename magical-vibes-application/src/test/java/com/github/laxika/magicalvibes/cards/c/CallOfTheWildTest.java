package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CallOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature card is put onto the battlefield")
    void creatureCardPutOntoBattlefield() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        Card creature = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(creature);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Revealed non-creature card is put into the graveyard")
    void nonCreatureCardPutIntoGraveyard() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
