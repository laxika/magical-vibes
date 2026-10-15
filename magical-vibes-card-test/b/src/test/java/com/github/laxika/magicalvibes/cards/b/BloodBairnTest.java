package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.r.RumblingBaloth;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodBairn.class, RumblingBaloth.class, CanyonMinotaur.class})
class BloodBairnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Blood Bairn +2/+2")
    void sacrificingAnotherCreatureBoosts() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new RumblingBaloth());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rumbling Baloth");
        Permanent bairn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.getCard().getName()).isEqualTo("Blood Bairn");
        assertThat(bairn.getEffectivePower()).isEqualTo(4);
        assertThat(bairn.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate when Blood Bairn is the only creature")
    void cannotSacrificeItself() {
        addBloodBairnReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood Bairn");
    }

    @Test
    @DisplayName("Choosing Blood Bairn itself as the sacrifice is rejected")
    void choosingItselfIsRejected() {
        Permanent bairn = addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new RumblingBaloth());
        harness.addToBattlefield(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bairn.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood Bairn");
        harness.assertOnBattlefield(player1, "Rumbling Baloth");
        harness.assertOnBattlefield(player1, "Canyon Minotaur");
    }

    @Test
    @DisplayName("Chosen creature is sacrificed when several are available")
    void chosenCreatureIsSacrificed() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new RumblingBaloth());
        harness.addToBattlefield(player1, new CanyonMinotaur());
        UUID minotaurId = harness.getPermanentId(player1, "Canyon Minotaur");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaurId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canyon Minotaur");
        harness.assertOnBattlefield(player1, "Rumbling Baloth");
        Permanent bairn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new RumblingBaloth());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bairn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.getEffectivePower()).isEqualTo(2);
        assertThat(bairn.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability costs no mana and does not tap Blood Bairn")
    void abilityIsFreeAndDoesNotTap() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new RumblingBaloth());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        Permanent bairn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid before the boost resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent bairn = harness.addToBattlefieldAndReturn(player1, new BloodBairn());
        harness.addToBattlefield(player1, new RumblingBaloth());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Rumbling Baloth");
        harness.assertNotOnBattlefield(player1, "Rumbling Baloth");
        assertThat(bairn.getEffectivePower()).isEqualTo(2);
        assertThat(bairn.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(bairn.getEffectivePower()).isEqualTo(4);
        assertThat(bairn.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated activations stack while Blood Bairn is tapped and summoning sick")
    void repeatedActivationsStackWithoutTapOrHasteRequirement() {
        Permanent bairn = harness.addToBattlefieldAndReturn(player1, new BloodBairn());
        bairn.tap();
        bairn.setSummoningSick(true);
        harness.addToBattlefield(player1, new RumblingBaloth());
        harness.addToBattlefield(player1, new CanyonMinotaur());
        UUID balothId = harness.getPermanentId(player1, "Rumbling Baloth");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, balothId);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rumbling Baloth");
        harness.assertInGraveyard(player1, "Canyon Minotaur");
        assertThat(bairn.getEffectivePower()).isEqualTo(6);
        assertThat(bairn.getEffectiveToughness()).isEqualTo(6);
        assertThat(bairn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player2, new RumblingBaloth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood Bairn");
        harness.assertOnBattlefield(player2, "Rumbling Baloth");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBloodBairnReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BloodBairn());
        perm.setSummoningSick(false);
        return perm;
    }
}
