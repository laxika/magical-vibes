package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.SunscapeFamiliar;
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
