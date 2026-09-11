package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallOfTheWild.class, GrizzlyBears.class})
class CallOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature card is put onto the battlefield")
    void creatureCardPutOntoBattlefield() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
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
        Card nonCreature = new CallOfTheWild();
        harness.setLibrary(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(nonCreature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(nonCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(nonCreature.getId()));
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the activating player's library and does not tap the enchantment")
    void usesActivatingPlayersLibraryWithoutTappingSource() {
        harness.addToBattlefield(player1, new CallOfTheWild());
        Card creature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setLibrary(player2, List.of(opponentCreature));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }
}
