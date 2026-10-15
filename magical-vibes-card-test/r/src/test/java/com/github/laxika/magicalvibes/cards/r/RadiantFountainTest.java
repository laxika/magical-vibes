package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantFountain.class})
class RadiantFountainTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains its controller 2 life")
    void entersGainingTwoLife() {
        harness.setHand(player1, List.of(new RadiantFountain()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapping for mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RadiantFountain());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The life gain uses the stack and benefits the second player's land controller")
    void secondPlayerGainsLifeOnlyWhenTriggerResolves() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RadiantFountain()));

        harness.playLand(player2, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The life gain resolves even after the land leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.setHand(player1, List.of(new RadiantFountain()));
        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Radiant Fountain");
        harness.assertInGraveyard(player1, "Radiant Fountain");
    }

    @Test
    @DisplayName("A newly played land can produce mana immediately while its life gain is pending")
    void manaAbilityResolvesImmediatelyWithEtbOnStack() {
        harness.setHand(player1, List.of(new RadiantFountain()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
