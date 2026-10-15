package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingCeratops.class, Cancel.class, Unsummon.class, Shock.class, AirElemental.class})
class ShiftingCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Shifting Ceratops cannot be countered by Cancel")
    void cannotBeCountered() {
        ShiftingCeratops ceratops = new ShiftingCeratops();
        harness.setHand(player1, List.of(ceratops));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ceratops.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shifting Ceratops");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Blue spells cannot target Shifting Ceratops")
    void cannotBeTargetedByBlueSpell() {
        Permanent ceratops = addCeratops(player2);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ceratops.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shifting Ceratops can choose each granted keyword")
    void choosesReachTrampleOrHaste() {
        Permanent ceratops = addCeratops(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        for (String keyword : List.of("REACH", "TRAMPLE", "HASTE")) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            harness.handleListChoice(player1, keyword);
            assertThat(gqs.hasKeyword(gd, ceratops, Keyword.valueOf(keyword))).isTrue();
        }
    }

    @Test
    @DisplayName("Shifting Ceratops loses its chosen keyword at end of turn")
    void chosenKeywordResetsAtEndOfTurn() {
        Permanent ceratops = addCeratops(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "TRAMPLE");

        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addCeratops(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ShiftingCeratops());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Protection from blue does not prevent red spell damage")
    void redSpellCanTargetAndDamage() {
        Permanent ceratops = addCeratops(player2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, ceratops.getId());
        harness.passBothPriorities();

        assertThat(ceratops.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Shifting Ceratops");
    }

    @Test
    @DisplayName("A single activation grants only the chosen keyword to its source")
    void grantsOnlyChosenKeywordToSource() {
        Permanent ceratops = addCeratops(player1);
        Permanent other = addCeratops(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "HASTE");

        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, ceratops, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Blue creatures cannot block Shifting Ceratops")
    void blueCreatureCannotBlock() {
        addCeratops(player1);
        harness.addToBattlefield(player2, new AirElemental());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Reach allows blocking a blue flyer and protection prevents its lethal damage")
    void reachBlocksBlueFlyerAndPreventsDamage() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        flyer.setSummoningSick(false);
        Permanent ceratops = addCeratops(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "REACH");

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Shifting Ceratops");
        assertThat(ceratops.getMarkedDamage()).isZero();
        assertThat(flyer.getMarkedDamage()).isEqualTo(5);
    }
}
