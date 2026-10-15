package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruulBeastmaster.class, SauroformHybrid.class, GrotesqueDemise.class})
class GruulBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Riot can put a +1/+1 counter on Gruul Beastmaster")
    void riotAddsCounter() {
        Permanent beastmaster = castBeastmaster(true);

        assertThat(beastmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack trigger targets another creature you control")
    void attackTriggerTargetsAnotherOwnCreature() {
        Permanent beastmaster = addCreatureReady(player1, new GruulBeastmaster());
        Permanent ownCreature = addCreatureReady(player1, new SauroformHybrid());
        Permanent opposingCreature = addCreatureReady(player2, new SauroformHybrid());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(ownCreature.getId())
                .doesNotContain(beastmaster.getId(), opposingCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target +X/+0 using Gruul Beastmaster's power")
    void attackBoostsUsingCurrentPower() {
        Permanent beastmaster = addCreatureReady(player1, new GruulBeastmaster());
        Permanent target = addCreatureReady(player1, new SauroformHybrid());
        beastmaster.setPowerModifier(2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Riot haste allows attacking immediately without a counter")
    void riotHasteAllowsImmediateAttack() {
        Permanent beastmaster = castBeastmaster(false);
        Permanent target = addCreatureReady(player1, new SauroformHybrid());

        assertThat(beastmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack boost uses power at resolution and stays fixed afterward")
    void boostUsesResolutionPower() {
        Permanent beastmaster = addCreatureReady(player1, new GruulBeastmaster());
        Permanent target = addCreatureReady(player1, new SauroformHybrid());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            beastmaster.setPowerModifier(3);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
            beastmaster.setPowerModifier(0);
            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Negative source power gives no power boost")
    void negativePowerIsZero() {
        Permanent beastmaster = addCreatureReady(player1, new GruulBeastmaster());
        Permanent target = addCreatureReady(player1, new SauroformHybrid());
        beastmaster.setPowerModifier(-3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack boost uses last known power after the source is exiled")
    void boostUsesLastKnownPower() {
        Permanent beastmaster = addCreatureReady(player1, new GruulBeastmaster());
        Permanent target = addCreatureReady(player1, new SauroformHybrid());
        beastmaster.setPowerModifier(1);
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, target.getId());
            harness.castInstant(player2, 0, beastmaster.getId());
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beastmaster);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        });
    }

    private Permanent castBeastmaster(boolean chooseCounter) {
        harness.setHand(player1, List.of(new GruulBeastmaster()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return findPermanent(player1, "Gruul Beastmaster");
    }
}
