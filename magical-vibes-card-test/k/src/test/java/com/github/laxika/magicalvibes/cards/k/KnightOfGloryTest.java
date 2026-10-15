package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AjanisSunstriker;
import com.github.laxika.magicalvibes.cards.c.CripplingBlight;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfGlory.class, AjanisSunstriker.class, WalkingCorpse.class, Murder.class, CripplingBlight.class})
class KnightOfGloryTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot block Knight of Glory")
    void blackCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new KnightOfGlory());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WalkingCorpse());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent knight = addCreatureReady(player2, new KnightOfGlory());
        addCreatureReady(player2, new AjanisSunstriker());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new KnightOfGlory());
        Permanent bears = addCreatureReady(player1, new AjanisSunstriker());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Knight attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent knight = addCreatureReady(player1, new KnightOfGlory());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new KnightOfGlory());
        Permanent bears = addCreatureReady(player1, new AjanisSunstriker());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new KnightOfGlory());
        Permanent bears = addCreatureReady(player1, new AjanisSunstriker());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Knight of Glory"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Protection prevents combat damage from a black creature when Knight blocks")
    void preventsBlackCombatDamage() {
        addCreatureReady(player1, new WalkingCorpse());
        Permanent knight = addCreatureReady(player2, new KnightOfGlory());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
        harness.assertInGraveyard(player1, "Walking Corpse");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A black Aura cannot enchant Knight of Glory")
    void blackAuraCannotTargetKnight() {
        Permanent knight = addCreatureReady(player2, new KnightOfGlory());
        addCreatureReady(player2, new AjanisSunstriker());
        harness.setHand(player1, List.of(new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Multiple exalted abilities each boost the lone attacker")
    void multipleExaltedAbilitiesStack() {
        addCreatureReady(player1, new KnightOfGlory());
        addCreatureReady(player1, new KnightOfGlory());
        Permanent attacker = addCreatureReady(player1, new AjanisSunstriker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(2));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent attacking alone does not trigger exalted")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new KnightOfGlory());
        Permanent attacker = addCreatureReady(player2, new AjanisSunstriker());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted still boosts an attacker removed from combat before resolution")
    void exaltedDoesNotRecheckCombatAtResolution() {
        addCreatureReady(player1, new KnightOfGlory());
        Permanent attacker = addCreatureReady(player1, new AjanisSunstriker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(1);
            attacker.setAttacking(false);
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
