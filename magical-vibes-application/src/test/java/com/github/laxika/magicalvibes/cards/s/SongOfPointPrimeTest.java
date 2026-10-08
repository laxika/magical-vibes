package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfPointPrime.class, CantorOfTheRefrain.class})
class SongOfPointPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature, then Cantor of the Refrain is conjured into the graveyard")
    void sacrificesAndConjuresCantor() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());
        castSong();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        harness.assertInGraveyard(player1, "Cantor of the Refrain");
    }

    @Test
    @DisplayName("The conjure clause resolves when an opponent controls no creatures")
    void conjuresWithoutAcreatureToSacrifice() {
        castSong();

        harness.assertInGraveyard(player1, "Cantor of the Refrain");
    }

    @Test
    void opponentChoosesWhichCreatureToSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());
        castSong();

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(first)
                .contains(second);
        harness.assertInGraveyard(player1, "Cantor of the Refrain");
    }

    @Test
    void doesNotSacrificeControllersCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CantorOfTheRefrain());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());

        castSong();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof CantorOfTheRefrain)
                .singleElement()
                .satisfies(card -> {
                    assertThat(card.getId()).isNotEqualTo(ownCreature.getCard().getId());
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    void conjuresOnlyAfterOpponentCompletesSacrificeChoice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CantorOfTheRefrain());

        castSong();

        harness.assertNotInGraveyard(player1, "Cantor of the Refrain");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof CantorOfTheRefrain)
                .singleElement()
                .satisfies(card -> {
                    assertThat(card.getId()).isNotEqualTo(second.getCard().getId());
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
    }

    private void castSong() {
        harness.castFromHand(player1, new SongOfPointPrime(), "{1}{B}");
        harness.passBothPriorities();
    }
}
