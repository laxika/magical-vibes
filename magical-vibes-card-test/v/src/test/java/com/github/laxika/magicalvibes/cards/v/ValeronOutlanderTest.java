package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CylianSunsinger;
import com.github.laxika.magicalvibes.cards.d.DragDown;
import com.github.laxika.magicalvibes.cards.e.ElderMastery;
import com.github.laxika.magicalvibes.cards.g.GrixisSlavedriver;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.cards.s.SuicidalCharge;
import com.github.laxika.magicalvibes.cards.w.WildLeotau;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValeronOutlander.class, CylianSunsinger.class, DragDown.class, ElderMastery.class,
        GrixisSlavedriver.class, PathToExile.class, SuicidalCharge.class, WildLeotau.class})
class ValeronOutlanderTest extends BaseCardTest {

    @Test
    @DisplayName("Black creature cannot block Valeron Outlander")
    void blackCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrixisSlavedriver());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Valeron Outlander")
    void greenCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Valeron Outlander takes no combat damage from black creature")
    void takesNoDamageFromBlack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrixisSlavedriver());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ValeronOutlander());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Grixis Slavedriver's 4 damage to Valeron is prevented by protection from black â€” it survives.
        harness.assertOnBattlefield(player2, "Valeron Outlander");
    }

    @Test
    @DisplayName("Valeron Outlander takes normal combat damage from green creature")
    void takesNormalDamageFromGreen() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WildLeotau());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ValeronOutlander());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // No protection from green â€” 5 damage kills the 2/2.
        harness.assertNotOnBattlefield(player2, "Valeron Outlander");
        harness.assertInGraveyard(player2, "Valeron Outlander");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent valeron = harness.addToBattlefieldAndReturn(player2, new ValeronOutlander());
        valeron.setSummoningSick(false);

        // Add valid target so spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new DragDown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, valeron.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent valeron = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        valeron.setSummoningSick(false);

        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, valeron.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Path to Exile");
    }

    @Test
    @DisplayName("A multicolored black Aura cannot target Valeron Outlander")
    void cannotBeTargetedByBlackAura() {
        Permanent valeron = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.setHand(player1, List.of(new ElderMastery()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, valeron.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A black Aura attached to Valeron Outlander is put into the graveyard")
    void blackAuraCannotRemainAttached() {
        Permanent valeron = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElderMastery());
        aura.setAttachedTo(valeron.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Valeron Outlander");
        harness.assertNotOnBattlefield(player1, "Elder Mastery");
        harness.assertInGraveyard(player1, "Elder Mastery");
    }

    @Test
    @DisplayName("Protection does not prevent a nontargeted black ability from reducing power and toughness")
    void nontargetedBlackAbilityStillAffectsCreature() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent valeron = harness.addToBattlefieldAndReturn(player2, new ValeronOutlander());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, valeron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, valeron)).isEqualTo(1);
        assertThat(valeron.isMustAttackThisTurn()).isTrue();
        harness.assertOnBattlefield(player2, "Valeron Outlander");
    }
}
