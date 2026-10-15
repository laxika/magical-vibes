package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.ScepterOfEmpires;
import com.github.laxika.magicalvibes.cards.t.ThroneOfEmpires;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrownOfEmpires.class, RuneclawBear.class, ScepterOfEmpires.class, ThroneOfEmpires.class})
class CrownOfEmpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Without both partners the ability only taps the target creature")
    void tapsTargetWithoutPartners() {
        addCreatureReady(player1, new CrownOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With only one partner the ability still just taps the target creature")
    void tapsTargetWithOnlyOnePartner() {
        addCreatureReady(player1, new CrownOfEmpires());
        addCreatureReady(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("With both partners you gain control of the target instead of tapping it")
    void gainsControlWithBothPartners() {
        addCreatureReady(player1, new CrownOfEmpires());
        addCreatureReady(player1, new ScepterOfEmpires());
        addCreatureReady(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new CrownOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new CrownOfEmpires());
        Permanent throne = addCreatureReady(player2, new ThroneOfEmpires());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, throne.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsControlWhenPartnersArriveBeforeResolution() {
        addCreatureReady(player1, new CrownOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        addCreatureReady(player1, new ScepterOfEmpires());
        addCreatureReady(player1, new ThroneOfEmpires());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void onlyTapsWhenPartnerLeavesBeforeResolution() {
        addCreatureReady(player1, new CrownOfEmpires());
        Permanent scepter = addCreatureReady(player1, new ScepterOfEmpires());
        addCreatureReady(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        gd.playerGraveyards.get(player1.getId()).add(scepter.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void opponentsPartnersDoNotEnableControlGain() {
        addCreatureReady(player1, new CrownOfEmpires());
        addCreatureReady(player2, new ScepterOfEmpires());
        addCreatureReady(player2, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void controlGainPreservesTappedStateAndSurvivesArtifactsLeaving() {
        Permanent crown = addCreatureReady(player1, new CrownOfEmpires());
        Permanent scepter = addCreatureReady(player1, new ScepterOfEmpires());
        Permanent throne = addCreatureReady(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.tap();
        scepter.tap();
        throne.tap();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        gd.playerBattlefields.get(player1.getId()).removeAll(java.util.List.of(crown, scepter, throne));
        gd.playerGraveyards.get(player1.getId()).addAll(java.util.List.of(
                crown.getCard(), scepter.getCard(), throne.getCard()));
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void abilityStillGainsControlIfCrownLeavesBeforeResolution() {
        Permanent crown = addCreatureReady(player1, new CrownOfEmpires());
        addCreatureReady(player1, new ScepterOfEmpires());
        addCreatureReady(player1, new ThroneOfEmpires());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(crown.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(crown);
        gd.playerGraveyards.get(player1.getId()).add(crown.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void cannotActivateTappedCrown() {
        Permanent crown = addCreatureReady(player1, new CrownOfEmpires());
        crown.tap();
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }
}
