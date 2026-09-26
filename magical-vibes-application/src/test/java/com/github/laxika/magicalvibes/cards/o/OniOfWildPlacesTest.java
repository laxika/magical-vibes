package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OniOfWildPlaces.class, AkkiUnderling.class, ArabaMothrider.class})
class OniOfWildPlacesTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new OniOfWildPlaces());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Offers only red creatures controlled by its controller")
    void offersOnlyRedCreaturesControlledByController() {
        Permanent oni = addCreatureReady(player1, new OniOfWildPlaces());
        Permanent redCreature = addCreatureReady(player1, new AkkiUnderling());
        Permanent nonRedCreature = addCreatureReady(player1, new ArabaMothrider());
        Permanent opponentRedCreature = addCreatureReady(player2, new AkkiUnderling());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds())
                .contains(oni.getId(), redCreature.getId())
                .doesNotContain(nonRedCreature.getId(), opponentRedCreature.getId());
    }

    @Test
    @DisplayName("Returns the chosen red creature to its owner's hand")
    void returnsChosenRedCreatureToOwnersHand() {
        addCreatureReady(player1, new OniOfWildPlaces());
        Permanent redCreature = addCreatureReady(player1, new AkkiUnderling());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, redCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(redCreature.getId()));
        harness.assertInHand(player1, "Akki Underling");
    }

    @Test
    @DisplayName("Returns a controlled red creature to its owner's hand")
    void returnsControlledRedCreatureToItsOwnersHand() {
        addCreatureReady(player1, new OniOfWildPlaces());
        AkkiUnderling redCreatureCard = new AkkiUnderling();
        redCreatureCard.setOwnerId(player2.getId());
        Permanent redCreature = addCreatureReady(player1, redCreatureCard);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, redCreature.getId());

        harness.assertNotInHand(player1, "Akki Underling");
        harness.assertInHand(player2, "Akki Underling");
    }

    @Test
    @DisplayName("Does nothing when no red creature remains when the ability resolves")
    void doesNothingWhenNoRedCreatureRemainsOnResolution() {
        Permanent oni = addCreatureReady(player1, new OniOfWildPlaces());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(oni);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
