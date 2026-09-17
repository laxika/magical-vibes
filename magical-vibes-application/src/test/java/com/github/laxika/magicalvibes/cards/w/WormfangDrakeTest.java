package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormfangDrake.class, SuntailHawk.class})
class WormfangDrakeTest extends BaseCardTest {

    private void castWormfangDrake() {
        harness.setHand(player1, List.of(new WormfangDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself when its controller has no other creature")
    void sacrificesItselfWithoutAnotherCreature() {
        castWormfangDrake();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertInGraveyard(player1, "Wormfang Drake");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifices itself when the only other creature is controlled by an opponent")
    void sacrificesItselfWithoutCreatureItControls() {
        harness.addToBattlefield(player2, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertInGraveyard(player1, "Wormfang Drake");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles another creature it controls")
    void exilesAnotherCreature() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hawk.getId());

        harness.assertOnBattlefield(player1, "Wormfang Drake");
        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Suntail Hawk"));
    }

    @Test
    @DisplayName("Returns the exiled creature when it leaves the battlefield")
    void returnsExiledCreatureWhenItLeaves() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        castWormfangDrake();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, hawk.getId());

        Permanent drake = findPermanent(player1, "Wormfang Drake");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, drake));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Suntail Hawk"));
    }
}
