package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.m.MindControl;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyodaiSoulOfKamigawa.class, DoomBlade.class, GrizzlyBears.class, EcologistsTerrarium.class, MindControl.class})
class KyodaiSoulOfKamigawaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants another permanent indestructible while Kyodai remains under your control")
    void grantsIndestructibleUntilKyodaiLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castKyodai(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        doomBlade(player1, bears.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        doomBlade(player1, harness.getPermanentId(player1, "Kyodai, Soul of Kamigawa"));
        doomBlade(player1, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kyodai cannot target itself with its enter-the-battlefield ability")
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new KyodaiSoulOfKamigawa()));
        addKyodaiMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kyodai, Soul of Kamigawa");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability gives Kyodai +5/+5 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent kyodai = addReadyKyodai(player1);
        addFiveColorMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kyodai.getPowerModifier()).isEqualTo(5);
        assertThat(kyodai.getToughnessModifier()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kyodai.getPowerModifier()).isZero();
        assertThat(kyodai.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's noncreature permanent can gain indestructible beyond the current turn")
    void protectsOpponentsArtifactAcrossTurns() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EcologistsTerrarium());
        castKyodai(player1, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Removing Kyodai before its trigger resolves prevents the indestructible grant")
    void noGrantWhenKyodaiLeavesBeforeTriggerResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castKyodai(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        doomBlade(player1, harness.getPermanentId(player1, "Kyodai, Soul of Kamigawa"));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
        doomBlade(player1, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Losing control of Kyodai ends the indestructible grant")
    void losingControlEndsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castKyodai(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MindControl()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player2, 0,
                harness.getPermanentId(player1, "Kyodai, Soul of Kamigawa"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kyodai, Soul of Kamigawa");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Kyodai's activated ability can be repeated while it is summoning sick")
    void repeatedActivationsStackWhileSummoningSick() {
        Permanent kyodai = harness.addToBattlefieldAndReturn(player1, new KyodaiSoulOfKamigawa());
        kyodai.setSummoningSick(true);
        addFiveColorMana(player1);
        addFiveColorMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kyodai.getPowerModifier()).isEqualTo(10);
        assertThat(kyodai.getToughnessModifier()).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kyodai.getPowerModifier()).isZero();
        assertThat(kyodai.getToughnessModifier()).isZero();
    }

    private Permanent addReadyKyodai(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KyodaiSoulOfKamigawa());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castKyodai(Player player, UUID targetId) {
        harness.setHand(player, List.of(new KyodaiSoulOfKamigawa()));
        addKyodaiMana(player);
        harness.castCreature(player, 0, 0, targetId);
    }

    private void addKyodaiMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void addFiveColorMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private void doomBlade(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DoomBlade()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
