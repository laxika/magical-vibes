package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.d.DuskUrchins;
import com.github.laxika.magicalvibes.cards.i.IntimidatorInitiate;
import com.github.laxika.magicalvibes.cards.a.AphoticWisps;
import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.c.ConsignToDream;
import com.github.laxika.magicalvibes.cards.c.CurseOfChains;
import com.github.laxika.magicalvibes.cards.f.Firespout;
import com.github.laxika.magicalvibes.cards.p.PunctureBolt;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OversoulOfDusk.class, DevotedDruid.class, BriarberryCohort.class, AphoticWisps.class,
        PunctureBolt.class, ConsignToDream.class, BarkshellBlessing.class, Firespout.class, CurseOfChains.class,
        DuskUrchins.class, IntimidatorInitiate.class})
class OversoulOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Oversoul of Dusk")
    void blueCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player2, new BriarberryCohort());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature can block Oversoul of Dusk")
    void greenCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DevotedDruid());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player2, new OversoulOfDusk());

        harness.setHand(player1, List.of(new AphoticWisps()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oversoul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by red instant")
    void cannotBeTargetedByRedInstant() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player2, new OversoulOfDusk());

        harness.setHand(player1, List.of(new PunctureBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oversoul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Cannot be targeted by blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player2, new OversoulOfDusk());

        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, oversoul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by green instant")
    void canBeTargetedByGreenInstant() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());

        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, oversoul.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(BarkshellBlessing.class);
    }

    @Test
    void preventsNonTargetedDamageFromRedGreenSpell() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player2, new OversoulOfDusk());
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.setHand(player1, List.of(new Firespout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(oversoul.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oversoul of Dusk");
        harness.assertInGraveyard(player2, "Devoted Druid");
    }

    @Test
    void cannotBeTargetedByWhiteBlueAuraEvenWhenPaidWithWhite() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        harness.setHand(player1, List.of(new CurseOfChains()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, oversoul.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void alreadyAttachedBlueAuraIsPutIntoGraveyard() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CurseOfChains());
        aura.setAttachedTo(oversoul.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Oversoul of Dusk");
        harness.assertNotOnBattlefield(player2, "Curse of Chains");
        harness.assertInGraveyard(player2, "Curse of Chains");
    }

    @Test
    void canBeTargetedByWhiteGreenSpellPaidWithWhite() {
        Permanent oversoul = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, oversoul.getId());

        assertThat(gqs.getEffectivePower(gd, oversoul)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, oversoul)).isEqualTo(7);
    }

    @Test
    void blackAndRedCreaturesCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OversoulOfDusk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new DuskUrchins());
        harness.addToBattlefield(player2, new IntimidatorInitiate());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        for (int blockerIndex = 0; blockerIndex < 2; blockerIndex++) {
            int index = blockerIndex;
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(index, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("protection");
        }
    }

    @Test
    void canBlockRedCreatureAndPreventsItsCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new IntimidatorInitiate());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent oversoul = harness.addToBattlefieldAndReturn(player2, new OversoulOfDusk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(oversoul.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oversoul of Dusk");
        harness.assertInGraveyard(player1, "Intimidator Initiate");
    }
}
