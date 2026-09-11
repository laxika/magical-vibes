package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Skinthinner.class, GrizzlyBears.class, ScatheZombies.class})
class SkinthinnerTest extends BaseCardTest {

    @Test
    void turningFaceUpDestroysTargetNonblackCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skinthinner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent skinthinner = findPermanent(player1, "Skinthinner");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skinthinner));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(skinthinner.isFaceDown()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotChooseBlackCreature() {
        Permanent zombies = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new Skinthinner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent skinthinner = findPermanent(player1, "Skinthinner");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(skinthinner));

        harness.passBothPriorities();

        assertThat(skinthinner.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(zombies);
    }
}
