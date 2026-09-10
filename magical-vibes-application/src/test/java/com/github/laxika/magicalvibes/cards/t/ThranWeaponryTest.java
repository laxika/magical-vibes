package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranWeaponry.class, YavimayaWurm.class})
class ThranWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability boosts all creatures on both battlefields")
    void activatedAbilityBoostsAllCreatures() {
        Permanent weaponry = addReadyWeaponry();
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();

        assertThat(weaponry.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost persists when the controller keeps the artifact tapped")
    void boostPersistsWhenWeaponryStaysTapped() {
        Permanent weaponry = addReadyWeaponry();
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(weaponry.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost is locked to creatures present when the ability resolves")
    void boostDoesNotAffectLaterCreatures() {
        Permanent weaponry = addReadyWeaponry();
        Permanent existingCreature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();
        Permanent laterCreature = addReadyCreature(player2);

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("The boost ends when the artifact becomes untapped")
    void boostEndsWhenWeaponryUntaps() {
        Permanent weaponry = addReadyWeaponry();
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(weaponry.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining echo sacrifices Thran Weaponry at its next upkeep")
    void decliningEchoSacrificesWeaponry() {
        castAndResolveWeaponry();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Thran Weaponry");
        harness.assertInGraveyard(player1, "Thran Weaponry");
    }

    @Test
    @DisplayName("Paying echo keeps Thran Weaponry and echo does not trigger again")
    void payingEchoKeepsWeaponryAndEchoIsOneShot() {
        castAndResolveWeaponry();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Thran Weaponry");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thran Weaponry");
    }

    private Permanent addReadyWeaponry() {
        Permanent weaponry = harness.addToBattlefieldAndReturn(player1, new ThranWeaponry());
        weaponry.setSummoningSick(false);
        return weaponry;
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new YavimayaWurm());
    }

    private void castAndResolveWeaponry() {
        harness.castFromHand(player1, new ThranWeaponry(), "{4}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thran Weaponry");
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
