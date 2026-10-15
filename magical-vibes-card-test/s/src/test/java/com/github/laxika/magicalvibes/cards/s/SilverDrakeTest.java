package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverDrake.class, StormscapeFamiliar.class, SunscapeFamiliar.class,
        AlphaKavu.class, CloudCover.class})
class SilverDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only white or blue creatures you control")
    void etbOffersMatchingCreaturesYouControl() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new AlphaKavu());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new CloudCover());
        Permanent opponentBlueCreature = harness.addToBattlefieldAndReturn(player2, new StormscapeFamiliar());

        castAndResolveSpell();

        UUID silverDrakeId = harness.getPermanentId(player1, "Silver Drake");
        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(whiteCreature.getId(), blueCreature.getId(), silverDrakeId)
                .doesNotContain(greenCreature.getId(), nonCreature.getId(), opponentBlueCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenMatchingCreatureReturnsToHand() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());

        castAndResolveSpell();
        UUID silverDrakeId = harness.getPermanentId(player1, "Silver Drake");
        resolveTriggerToChoice();
        harness.handlePermanentChosen(player1, whiteCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(whiteCreature.getId()))
                .anyMatch(permanent -> permanent.getId().equals(silverDrakeId));
        assertThat(gd.playerHands.get(player1.getId())).contains(whiteCreature.getCard());
    }

    @Test
    @DisplayName("The Drake can return itself when it is the only matching creature")
    void canReturnItself() {
        harness.addToBattlefield(player1, new AlphaKavu());

        SilverDrake silverDrake = castAndResolveSpell();
        UUID silverDrakeId = harness.getPermanentId(player1, "Silver Drake");
        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(silverDrakeId);

        harness.handlePermanentChosen(player1, silverDrakeId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(silverDrakeId));
        assertThat(gd.playerHands.get(player1.getId())).contains(silverDrake);
    }

    @Test
    @DisplayName("A controlled creature returns to its owner rather than its controller")
    void returnsCreatureToItsOwner() {
        SunscapeFamiliar stolenCard = new SunscapeFamiliar();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, stolenCard);

        castAndResolveSpell();
        resolveTriggerToChoice();
        harness.handlePermanentChosen(player1, stolenCreature.getId());

        assertThat(gd.playerHands.get(player2.getId())).contains(stolenCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(stolenCard);
        harness.assertNotOnBattlefield(player1, "Sunscape Familiar");
        harness.assertOnBattlefield(player1, "Silver Drake");
    }

    @Test
    @DisplayName("The return still happens after Silver Drake leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar());
        castAndResolveSpell();
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SilverDrake)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, drake));

        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(blueCreature.getId());
        harness.handlePermanentChosen(player1, blueCreature.getId());
        harness.assertInHand(player1, "Stormscape Familiar");
        harness.assertNotOnBattlefield(player1, "Stormscape Familiar");
        harness.assertInGraveyard(player1, "Silver Drake");
    }

    @Test
    @DisplayName("The return does nothing if no matching creatures remain at resolution")
    void noMatchingCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AlphaKavu());
        castAndResolveSpell();
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SilverDrake)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, drake));

        resolveTriggerToChoice();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Alpha Kavu");
        harness.assertNotInHand(player1, "Alpha Kavu");
        harness.assertInGraveyard(player1, "Silver Drake");
    }

    private SilverDrake castAndResolveSpell() {
        SilverDrake silverDrake = new SilverDrake();
        harness.castFromHand(player1, silverDrake, "{1}{W}{U}");
        harness.passBothPriorities();
        return silverDrake;
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }

}
