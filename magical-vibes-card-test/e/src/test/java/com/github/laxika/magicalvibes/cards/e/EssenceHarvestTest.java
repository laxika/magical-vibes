package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DeathsShadow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceHarvest.class, GrizzlyBears.class, HillGiant.class, DeathsShadow.class})
class EssenceHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("X stays fixed when losing life changes the greatest power")
    void selfTargetWithLifeDependentPowerUsesSameXForBothEffects() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new DeathsShadow());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("X uses the greatest power at resolution rather than casting")
    void usesPowerAtResolution() {
        harness.setLife(player1, 12);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DeathsShadow());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.setLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Targeting yourself does not cause a loss before the life gain finishes")
    void selfTargetCanTemporarilyReachZeroLife() {
        harness.setLife(player1, 3);
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 3);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Essence Harvest");
    }

    @Test
    @DisplayName("Essence Harvest drains for the greatest power among creatures you control")
    void drainsForGreatestPower() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Essence Harvest ignores creatures controlled by the opponent")
    void ignoresOpponentCreatures() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Essence Harvest does nothing when you control no creatures")
    void noCreaturesMeansNoDrain() {
        harness.setLife(player1, 16);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Essence Harvest can target its controller")
    void canTargetSelf() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Essence Harvest cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Essence Harvest goes to the graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EssenceHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Essence Harvest");
    }
}
