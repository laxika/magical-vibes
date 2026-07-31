package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodBairnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Blood Bairn +2/+2")
    void sacrificingAnotherCreatureBoosts() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bairn.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Blood Bairn");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Chosen creature is sacrificed when several are available")
    void chosenCreatureIsSacrificed() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent bairn = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off during cleanup")
    void boostWearsOffAtEndOfTurn() {
        addBloodBairnReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

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
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        Permanent bairn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bairn.isTapped()).isFalse();
    }

    private Permanent addBloodBairnReady(Player player) {
        Permanent perm = new Permanent(new BloodBairn());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
