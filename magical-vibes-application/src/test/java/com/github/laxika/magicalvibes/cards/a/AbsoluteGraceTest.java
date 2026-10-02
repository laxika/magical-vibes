package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbsoluteGrace.class, GorillaWarrior.class, Opalescence.class, Pestilence.class})
class AbsoluteGraceTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have protection from black")
    void grantsProtectionFromBlackToAllCreatures() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        Permanent ownGorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent opponentGorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThat(gqs.hasProtectionFrom(gd, ownGorilla, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opponentGorilla, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownGorilla, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Protection from black ends when Absolute Grace leaves the battlefield")
    void protectionEndsWhenAbsoluteGraceLeaves() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        assertThat(gqs.hasProtectionFrom(gd, gorilla, CardColor.BLACK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Absolute Grace"));

        assertThat(gqs.hasProtectionFrom(gd, gorilla, CardColor.BLACK)).isFalse();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Absolute Grace has protection from black when it becomes a creature")
    void animatedAbsoluteGraceHasProtectionFromBlack() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent grace = harness.addToBattlefieldAndReturn(player1, new AbsoluteGrace());

        assertThat(gqs.isCreature(gd, grace)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, grace, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Noncreature enchantments do not gain protection from black")
    void noncreatureEnchantmentsDoNotGainProtection() {
        Permanent grace = harness.addToBattlefieldAndReturn(player1, new AbsoluteGrace());
        Permanent pestilence = harness.addToBattlefieldAndReturn(player2, new Pestilence());

        assertThat(gqs.hasProtectionFrom(gd, grace, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, pestilence, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering later gain protection immediately")
    void laterCreaturesGainProtection() {
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.runStateBasedActions();

        Permanent gorilla = harness.enterBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThat(gqs.hasProtectionFrom(gd, gorilla, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Black damage is prevented for both players' creatures but not players")
    void preventsBlackDamageToAllCreatures() {
        harness.addToBattlefield(player1, new Pestilence());
        harness.addToBattlefield(player1, new AbsoluteGrace());
        Permanent ownGorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent opponentGorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownGorilla.getMarkedDamage()).isZero();
        assertThat(opponentGorilla.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
