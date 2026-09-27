package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnforgivingOvertake.class, AirResponseUnit.class, FountainOfYouth.class, GrizzlyBears.class})
class UnforgivingOvertakeTest extends BaseCardTest {

    @Test
    @DisplayName("A non-starting player pays one less to cast Unforgiving Overtake")
    void nonStartingPlayerGetsCostReduction() {
        forceNonStartingPlayer();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UnforgivingOvertake spell = new UnforgivingOvertake();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThat(harness.getCastingCostService().getCastCostModifier(gd, player2.getId(), spell))
                .isEqualTo(-1);

        harness.castSorcery(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The starting player does not get the non-starting-player cost reduction")
    void startingPlayerDoesNotGetCostReduction() {
        harness.forceActivePlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnforgivingOvertake()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature and its controller loses 2 life")
    void destroysCreatureAndItsControllerLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        castForNonStartingPlayer(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Destroys a noncreature Vehicle")
    void destroysVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        castForNonStartingPlayer(target);

        harness.assertNotOnBattlefield(player1, "Air Response Unit");
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player2, List.of(new UnforgivingOvertake()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    private void castForNonStartingPlayer(Permanent target) {
        forceNonStartingPlayer();
        harness.setHand(player2, List.of(new UnforgivingOvertake()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void forceNonStartingPlayer() {
        harness.forceActivePlayer(player2);
        gd.startingPlayerId = player1.getId();
    }
}
