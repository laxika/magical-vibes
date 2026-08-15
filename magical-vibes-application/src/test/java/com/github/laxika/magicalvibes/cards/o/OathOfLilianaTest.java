package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OathOfLilianaTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a creature of their choice")
    void eachOpponentSacrificesCreatureOnEnter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new OathOfLiliana()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Giant Spider"));

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("At each end step, creates a Zombie if a planeswalker entered under its controller's control this turn")
    void createsZombieAfterPlaneswalkerEnters() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.setHand(player1, List.of(new LilianaTheLastHope()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not create a Zombie when only a creature entered under its controller's control")
    void doesNotCreateZombieForCreatureEntry() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
