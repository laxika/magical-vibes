package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralColony.class, GrizzlyBears.class, WallOfVines.class})
class CoralColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the target player for the number of defender creatures you control")
    void millsForControlledDefenderCount() {
        addCreatureReady(player1, new CoralColony());
        addCreatureReady(player1, new WallOfVines());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfVines());

        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(library.get(0), library.get(1));
    }
}
