package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfPointPrime.class, CantorOfTheRefrain.class, GrizzlyBears.class})
class SongOfPointPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature, then Cantor of the Refrain is conjured into the graveyard")
    void sacrificesAndConjuresCantor() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSong();

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(first)
                .contains(second);
        harness.assertInGraveyard(player1, "Cantor of the Refrain");
    }

    private void castSong() {
        harness.setHand(player1, List.of(new SongOfPointPrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
