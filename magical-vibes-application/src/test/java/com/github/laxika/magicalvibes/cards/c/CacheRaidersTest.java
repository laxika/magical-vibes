package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HelixPinnacle;
import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacheRaiders.class, WickerboughElder.class, HelixPinnacle.class})
class CacheRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents a mandatory permanent choice only during resolution")
    void upkeepTriggerPresentsChoice() {
        addCreatureReady(player1, new CacheRaiders());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Chosen controlled creature is returned to its owner's hand")
    void returnsChosenControlledCreatureToHand() {
        addCreatureReady(player1, new CacheRaiders());
        Permanent elder = addCreatureReady(player1, new WickerboughElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elder.getId());

        harness.assertNotOnBattlefield(player1, "Wickerbough Elder");
        harness.assertInHand(player1, "Wickerbough Elder");
        harness.assertNotInGraveyard(player1, "Wickerbough Elder");
    }

    @Test
    @DisplayName("Can be forced to return itself when it is the only permanent")
    void returnsItselfWhenOnlyPermanent() {
        Permanent raiders = addCreatureReady(player1, new CacheRaiders());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, raiders.getId());

        harness.assertNotOnBattlefield(player1, "Cache Raiders");
        harness.assertInHand(player1, "Cache Raiders");
    }

    @Test
    @DisplayName("Only permanents the controller controls are legal choices")
    void onlyControlledPermanentsAreLegal() {
        Permanent raiders = addCreatureReady(player1, new CacheRaiders());
        Permanent opponentElder = addCreatureReady(player2, new WickerboughElder());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds())
                .contains(raiders.getId())
                .doesNotContain(opponentElder.getId());
    }

    @Test
    @CardUsed({CacheRaiders.class, HelixPinnacle.class})
    @DisplayName("Can return a shrouded permanent because the ability does not target")
    void canReturnShroudedPermanent() {
        addCreatureReady(player1, new CacheRaiders());
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(pinnacle.getId());

        harness.handlePermanentChosen(player1, pinnacle.getId());

        harness.assertNotOnBattlefield(player1, "Helix Pinnacle");
        harness.assertInHand(player1, "Helix Pinnacle");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new CacheRaiders());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Cache Raiders");
    }

    @Test
    @DisplayName("Returns a controlled permanent to its owner rather than its controller")
    void returnsBorrowedPermanentToOwnersHand() {
        addCreatureReady(player1, new CacheRaiders());
        Permanent elder = addCreatureReady(player2, new WickerboughElder());
        gd.playerBattlefields.get(player2.getId()).remove(elder);
        gd.playerBattlefields.get(player1.getId()).add(elder);
        gd.stolenCreatures.put(elder.getId(), player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elder.getId());

        harness.assertNotOnBattlefield(player1, "Wickerbough Elder");
        harness.assertInHand(player2, "Wickerbough Elder");
        harness.assertNotInHand(player1, "Wickerbough Elder");
    }
}
