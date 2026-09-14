package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElshaOfTheInfinite.class, Divination.class, Forest.class, GrizzlyBears.class})
class ElshaOfTheInfiniteTest extends BaseCardTest {

    @Test
    void castsNoncreatureSpellFromLibraryTopWithFlashAndTriggersProwess() {
        Permanent elsha = harness.addToBattlefieldAndReturn(player1, new ElshaOfTheInfinite());
        Divination divination = new Divination();
        harness.setLibrary(player1, List.of(divination, new Forest(), new Forest()));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elsha)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elsha)).isEqualTo(4);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 2);
    }

    @Test
    void topLibraryPermissionDoesNotGiveFlashToHandSpells() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureOnLibraryTopCannotBeCast() {
        harness.addToBattlefield(player1, new ElshaOfTheInfinite());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }
}
