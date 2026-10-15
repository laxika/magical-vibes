package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.s.SealOfPrimordium;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceWarden.class, CitanulWoodreaders.class, SealOfPrimordium.class})
class EssenceWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature enters")
    void gainsLifeWhenAnotherCreatureEnters() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new CitanulWoodreaders(), "{2}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains 1 life when an opponent's creature enters")
    void gainsLifeWhenOpponentsCreatureEnters() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CitanulWoodreaders(), "{2}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger when a noncreature permanent enters")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player1, new SealOfPrimordium());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when Essence Warden itself enters")
    void doesNotTriggerForItself() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EssenceWarden(), "{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Warden triggers the first but not itself")
    void secondWardenTriggersOnlyTheFirst() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new EssenceWarden(), "{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);

        harness.castFromHand(player1, new CitanulWoodreaders(), "{2}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Each Warden's controller gains life when a creature enters")
    void wardensWithDifferentControllersBothTrigger() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.addToBattlefield(player2, new EssenceWarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new CitanulWoodreaders(), "{2}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Life gain waits for resolution and survives the Warden leaving")
    void pendingTriggerResolvesAfterWardenDies() {
        var warden = harness.addToBattlefieldAndReturn(player1, new EssenceWarden());
        harness.setLife(player1, 20);

        harness.enterBattlefieldAndReturn(player2, new CitanulWoodreaders());
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        warden.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Essence Warden");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }
}
