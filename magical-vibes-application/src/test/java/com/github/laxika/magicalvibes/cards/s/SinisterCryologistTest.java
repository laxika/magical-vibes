package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterCryologist.class})
class SinisterCryologistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -3/-0 until end of turn")
    void etbDebuffsOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        castAndResolve(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SinisterCryologist());
        harness.setHand(player1, List.of(new SinisterCryologist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("The -3/-0 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        castAndResolve(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Warp casts for {U}, applies its ETB ability, and exiles at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        SinisterCryologist cryologist = new SinisterCryologist();
        harness.setHand(player1, List.of(cryologist));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cryologist.getId())).isNotNull();
    }

    @Test
    @DisplayName("A normal cast remains on the battlefield at the end step")
    void normalCastIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        castAndResolve(target.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Sinister Cryologist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Warp permits a later normal-cost cast from exile with a new ETB ability")
    void warpedCardCanBeCastFromExileOnLaterTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SinisterCryologist());
        SinisterCryologist cryologist = new SinisterCryologist();
        harness.setHand(player1, List.of(cryologist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Sinister Cryologist");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(cryologist.getId())).isNotNull();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, cryologist.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, cryologist.getId(), target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cryologist.getId())).isNull();
        harness.assertOnBattlefield(player1, "Sinister Cryologist");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Sinister Cryologist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The creature can enter when the opponent has no creatures")
    void canCastWithoutLegalEtbTarget() {
        harness.castFromHand(player1, new SinisterCryologist(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sinister Cryologist");
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SinisterCryologist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
