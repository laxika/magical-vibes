package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DualcasterMage.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class DualcasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a sorcery spell when it enters the battlefield")
    void copiesSorceryOnEnter() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();

        harness.setHand(player1, List.of(counsel));
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, counsel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player2.getId()))
                .containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Does not trigger when no instant or sorcery spell is on the stack")
    void doesNotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DualcasterMage()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
