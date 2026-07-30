package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrimsonMuckwaderTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/1 without a Swamp")
    void noBoostWithoutSwamp() {
        harness.addToBattlefield(player1, new CrimsonMuckwader());
        harness.addToBattlefield(player1, new Forest());

        Permanent muckwader = findPermanent(player1, "Crimson Muckwader");
        assertThat(gqs.getEffectivePower(gd, muckwader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, muckwader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 while its controller controls a Swamp")
    void boostWithSwamp() {
        harness.addToBattlefield(player1, new CrimsonMuckwader());
        harness.addToBattlefield(player1, new Swamp());

        Permanent muckwader = findPermanent(player1, "Crimson Muckwader");
        assertThat(gqs.getEffectivePower(gd, muckwader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, muckwader)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Swamp does not grant the boost")
    void noBoostFromOpponentSwamp() {
        harness.addToBattlefield(player1, new CrimsonMuckwader());
        harness.addToBattlefield(player2, new Swamp());

        Permanent muckwader = findPermanent(player1, "Crimson Muckwader");
        assertThat(gqs.getEffectivePower(gd, muckwader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, muckwader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addMuckwaderReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent muckwader = findPermanent(player1, "Crimson Muckwader");
        assertThat(muckwader.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addMuckwaderReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crimson Muckwader");
        Permanent muckwader = findPermanent(player1, "Crimson Muckwader");
        assertThat(muckwader.isTapped()).isTrue();
        assertThat(muckwader.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Dies to lethal combat damage without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addMuckwaderReady(player1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crimson Muckwader");
        harness.assertInGraveyard(player1, "Crimson Muckwader");
    }

    private Permanent addMuckwaderReady(Player player) {
        Permanent perm = new Permanent(new CrimsonMuckwader());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
