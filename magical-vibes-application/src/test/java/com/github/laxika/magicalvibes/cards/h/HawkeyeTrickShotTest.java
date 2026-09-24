package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeTrickShot.class, LukeCagePowerMan.class, GrizzlyBears.class})
class HawkeyeTrickShotTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry deals damage equal to the number of Heroes controlled")
    void ownEntryDealsDamageBasedOnHeroCount() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LukeCagePowerMan());

        harness.setHand(player1, List.of(new HawkeyeTrickShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Another Hero entering deals damage based on the updated Hero count")
    void anotherHeroEntryDealsDamageBasedOnHeroCount() {
        harness.addToBattlefield(player1, new HawkeyeTrickShot());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LukeCagePowerMan()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Hero creature entering does not trigger Hawkeye")
    void nonHeroEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HawkeyeTrickShot());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
