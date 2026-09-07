package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShapeshiftersMarrow.class, GrizzlyBears.class, Island.class})
class ShapeshiftersMarrowTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a revealed creature and permanently copies it")
    void millsCreatureAndBecomesPermanentCopy() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card creature = new GrizzlyBears();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(creature, nextCard));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gqs.isCreature(gd, marrow)).isTrue();
        assertThat(gqs.getEffectivePower(gd, marrow)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marrow)).isEqualTo(2);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Leaves a revealed noncreature on top and keeps its ability")
    void leavesNoncreatureOnTop() {
        Permanent marrow = harness.addToBattlefieldAndReturn(player1, new ShapeshiftersMarrow());
        Card land = new Island();
        harness.setLibrary(player2, List.of(land));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, marrow)).isFalse();

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
    }
}
