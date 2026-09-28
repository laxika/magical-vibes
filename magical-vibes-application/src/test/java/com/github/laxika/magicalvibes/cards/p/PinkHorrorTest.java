package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PinkHorror.class, DarkRitual.class, DoomBlade.class})
class PinkHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a chosen target when you cast an instant")
    void instantCastTriggerDealsDamage() {
        harness.addToBattlefield(player1, new PinkHorror());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("When it dies, creates two Blue Horror tokens with the spell-cast ability")
    void deathCreatesBlueHorrors() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Blue Horror")).hasSize(2);
        assertThat(findPermanents(player1, "Pink Horror")).isEmpty();
    }

    @Test
    @DisplayName("Blue Horrors each deal 1 damage when you cast an instant")
    void blueHorrorsTriggerOnInstantCast() {
        Permanent pinkHorror = harness.addToBattlefieldAndReturn(player1, new PinkHorror());
        killPinkHorror(pinkHorror);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    private void killPinkHorror(Permanent pinkHorror) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, pinkHorror.getId());
        harness.passBothPriorities();
    }
}
