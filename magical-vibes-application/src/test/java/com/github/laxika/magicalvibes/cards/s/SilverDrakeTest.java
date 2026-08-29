package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.SunscapeFamiliar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SilverDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only white or blue creatures you control")
    void etbOffersMatchingCreaturesYouControl() {
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar());
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new AlphaKavu());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new CloudCover());
        Permanent opponentWhiteCreature = harness.addToBattlefieldAndReturn(player2, new StormscapeFamiliar());

        SilverDrake silverDrake = castAndResolveSpell();

        UUID silverDrakeId = permanentIdFor(silverDrake);
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(whiteCreature.getId(), blueCreature.getId(), silverDrakeId)
                .doesNotContain(greenCreature.getId(), nonCreature.getId(), opponentWhiteCreature.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenMatchingCreatureReturnsToHand() {
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new SunscapeFamiliar());

        SilverDrake silverDrake = castAndResolveSpell();
        UUID silverDrakeId = permanentIdFor(silverDrake);
        resolveTriggerToChoice();
        harness.handlePermanentChosen(player1, blueCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blueCreature.getId()))
                .anyMatch(permanent -> permanent.getId().equals(silverDrakeId));
        assertThat(gd.playerHands.get(player1.getId())).contains(blueCreature.getCard());
    }

    @Test
    @DisplayName("The Drake can return itself when it is the only matching creature")
    void canReturnItself() {
        harness.addToBattlefield(player1, new AlphaKavu());

        SilverDrake silverDrake = castAndResolveSpell();
        UUID silverDrakeId = permanentIdFor(silverDrake);
        resolveTriggerToChoice();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(silverDrakeId);

        harness.handlePermanentChosen(player1, silverDrakeId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(silverDrakeId));
        assertThat(gd.playerHands.get(player1.getId())).contains(silverDrake);
    }

    private SilverDrake castAndResolveSpell() {
        SilverDrake silverDrake = new SilverDrake();
        harness.setHand(player1, List.of(silverDrake));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return silverDrake;
    }

    private void resolveTriggerToChoice() {
        harness.passBothPriorities();
    }

    private UUID permanentIdFor(SilverDrake silverDrake) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == silverDrake)
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
