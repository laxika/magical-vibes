package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BrackwaterElemental;
import com.github.laxika.magicalvibes.cards.c.ControlledInstincts;
import com.github.laxika.magicalvibes.cards.c.CylianSunsinger;
import com.github.laxika.magicalvibes.cards.e.EsperCormorants;
import com.github.laxika.magicalvibes.cards.f.FieryFall;
import com.github.laxika.magicalvibes.cards.c.ConstrictingTendrils;
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

@CardUsed({NacatlOutlander.class, BrackwaterElemental.class, CylianSunsinger.class,
        EsperCormorants.class, ConstrictingTendrils.class, FieryFall.class, ControlledInstincts.class})
class NacatlOutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Nacatl Outlander")
    void blueCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new BrackwaterElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Nacatl Outlander")
    void greenCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Nacatl Outlander takes no combat damage from a blue creature")
    void takesNoDamageFromBlue() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BrackwaterElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NacatlOutlander());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Nacatl Outlander");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player2, new NacatlOutlander());
        harness.addToBattlefield(player2, new CylianSunsinger());
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nacatl.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, nacatl.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fiery Fall");
    }

    @Test
    @DisplayName("Protection from blue does not prevent red spell damage")
    void redSpellDamageIsNotPrevented() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player2, new NacatlOutlander());
        harness.setHand(player1, List.of(new FieryFall()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, nacatl.getId());

        harness.assertInGraveyard(player2, "Nacatl Outlander");
        harness.assertNotOnBattlefield(player2, "Nacatl Outlander");
    }

    @Test
    @DisplayName("A white and blue creature cannot block Nacatl Outlander")
    void multicoloredBlueCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new EsperCormorants());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection prevents the controller's blue Aura from targeting Nacatl Outlander")
    void ownBlueAuraCannotTarget() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.setHand(player1, List.of(new ControlledInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nacatl.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An attached blue Aura goes to the graveyard as a state-based action")
    void attachedBlueAuraIsRemoved() {
        Permanent nacatl = harness.addToBattlefieldAndReturn(player1, new NacatlOutlander());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ControlledInstincts());
        aura.setAttachedTo(nacatl.getId());

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Controlled Instincts");
        harness.assertNotOnBattlefield(player1, "Controlled Instincts");
        harness.assertOnBattlefield(player1, "Nacatl Outlander");
    }
}
