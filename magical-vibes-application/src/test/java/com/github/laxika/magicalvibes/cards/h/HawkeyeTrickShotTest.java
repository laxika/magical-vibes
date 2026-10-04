package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LukeCagePowerMan;
import com.github.laxika.magicalvibes.cards.s.ShuriTheBlackPanther;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeTrickShot.class, LukeCagePowerMan.class, GrizzlyBears.class,
        ShuriTheBlackPanther.class, SwordsToPlowshares.class})
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
        harness.handlePermanentChosen(player1, player2.getId());
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
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hawkeye entering alone deals one damage and ignores opposing Heroes")
    void ownEntryCountsOnlyControlledHeroes() {
        harness.addToBattlefield(player2, new ShuriTheBlackPanther());
        harness.setHand(player1, List.of(new HawkeyeTrickShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The entering Hero deals the damage and its lifelink gains life")
    void enteringHeroIsDamageSource() {
        harness.addToBattlefield(player1, new HawkeyeTrickShot());
        harness.setHand(player1, List.of(new ShuriTheBlackPanther()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An opposing Hero entering does not trigger Hawkeye")
    void opposingHeroEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new HawkeyeTrickShot());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ShuriTheBlackPanther()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Hero count is evaluated at resolution even after Hawkeye leaves")
    void heroCountUsesResolutionStateAfterHawkeyeLeaves() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeTrickShot());
        harness.setHand(player1, List.of(new ShuriTheBlackPanther(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player1, 0, hawkeye.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hawkeye, Trick Shot");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("An entering Hero that leaves still deals damage using its last known lifelink")
    void departedEnteringHeroRetainsLifelinkForDamage() {
        harness.addToBattlefield(player1, new HawkeyeTrickShot());
        harness.setHand(player1, List.of(new ShuriTheBlackPanther(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Shuri, the Black Panther"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shuri, the Black Panther");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 23);
    }
}
