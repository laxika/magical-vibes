package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KhenraScrapper;
import com.github.laxika.magicalvibes.cards.o.OpenFire;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AccursedHorde.class, KhenraScrapper.class, OpenFire.class})
class AccursedHordeTest extends BaseCardTest {

    private void addHorde() {
        addCreatureReady(player1, new AccursedHorde());
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Grants indestructible to an attacking Zombie, then it wears off at end of turn")
    void grantsIndestructibleToAttackingZombie() {
        addHorde();
        Permanent attackingZombie = addCreatureReady(player1, new AccursedHorde());
        attackingZombie.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attackingZombie.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attackingZombie, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a Zombie that is not attacking")
    void cannotTargetNonAttackingZombie() {
        addHorde();
        Permanent idleZombie = addCreatureReady(player1, new AccursedHorde());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idleZombie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking non-Zombie creature")
    void cannotTargetAttackingNonZombie() {
        addHorde();
        Permanent attackingCreature = addCreatureReady(player1, new KhenraScrapper());
        attackingCreature.setAttacking(true);
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, attackingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacking Horde can target itself while tapped")
    void canTargetItselfWhileTapped() {
        Permanent horde = addCreatureReady(player1, new AccursedHorde());
        horde.setAttacking(true);
        horde.setTapped(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, horde.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horde, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(horde.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Horde can protect an opponent's attacking Zombie")
    void canProtectOpponentsAttackingZombieWhileSummoningSick() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent horde = harness.addToBattlefieldAndReturn(player1, new AccursedHorde());
        horde.setSummoningSick(true);
        Permanent attacker = addCreatureReady(player2, new AccursedHorde());
        attacker.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, horde, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability does not resolve if its target stops attacking")
    void targetMustStillBeAttackingAtResolution() {
        addHorde();
        Permanent attacker = addCreatureReady(player1, new AccursedHorde());
        attacker.setAttacking(true);
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability requires black mana, not just two generic mana")
    void cannotActivateWithoutBlackMana() {
        Permanent horde = addCreatureReady(player1, new AccursedHorde());
        horde.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, horde.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, horde, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A protected attacking Zombie survives lethal damage")
    void protectedZombieSurvivesLethalDamage() {
        Permanent horde = addCreatureReady(player1, new AccursedHorde());
        horde.setAttacking(true);
        addManaForAbility();
        harness.activateAbility(player1, 0, 0, null, horde.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new OpenFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, horde.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(horde);
        assertThat(horde.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, horde, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
