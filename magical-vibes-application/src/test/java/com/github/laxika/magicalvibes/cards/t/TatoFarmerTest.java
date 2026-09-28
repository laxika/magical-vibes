package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatoFarmer.class, Forest.class, PersistentPetitioners.class})
class TatoFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may give its controller two rad counters")
    void landfallMayGiveTwoRadCounters() {
        harness.addToBattlefield(player1, new TatoFarmer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining landfall gives no rad counters")
    void decliningLandfallGivesNoRadCounters() {
        harness.addToBattlefield(player1, new TatoFarmer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    @Test
    @DisplayName("Tap ability returns a milled land from any graveyard tapped")
    void returnsMilledLandTappedUnderControllerControl() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);

        for (int i = 0; i < 4; i++) {
            Permanent petitioners = harness.addToBattlefieldAndReturn(player2, new PersistentPetitioners());
            petitioners.setSummoningSick(false);
        }
        Forest milledLand = new Forest();
        harness.setLibrary(player2, List.of(milledLand));
        harness.activateAbility(player2, 3, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, milledLand.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(milledLand.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(milledLand);
    }

    @Test
    @DisplayName("Tap ability cannot target a land that was not milled this turn")
    void cannotTargetLandNotMilledThisTurn() {
        harness.addToBattlefield(player1, new TatoFarmer());
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
