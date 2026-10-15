package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WretchedAnurid.class, GlorySeeker.class, Shock.class})
class WretchedAnuridTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control causes you to lose 1 life")
    void triggersForYourCreature() {
        harness.addToBattlefield(player1, new WretchedAnurid());
        harness.setHand(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("An opponent's creature entering causes the Anurid's controller to lose 1 life")
    void triggersForOpponentsCreature() {
        harness.addToBattlefield(player1, new WretchedAnurid());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GlorySeeker()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Wretched Anurid triggers when another creature enters")
    void eachAnuridTriggersSeparately() {
        harness.addToBattlefield(player1, new WretchedAnurid());
        harness.addToBattlefield(player1, new WretchedAnurid());
        harness.setHand(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The Anurid does not trigger when it enters")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new WretchedAnurid()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An entering Anurid triggers an existing opponent's Anurid but not itself")
    void enteringAnuridTriggersOnlyExistingAnurid() {
        harness.addToBattlefield(player1, new WretchedAnurid());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WretchedAnurid()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A pending life-loss trigger still resolves after the Anurid dies")
    void triggerResolvesAfterSourceDies() {
        var anurid = harness.addToBattlefieldAndReturn(player1, new WretchedAnurid());
        harness.setHand(player1, List.of(new GlorySeeker(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, anurid.getId());
        harness.castAndResolveInstant(player1, 0, anurid.getId());

        harness.assertInGraveyard(player1, "Wretched Anurid");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
