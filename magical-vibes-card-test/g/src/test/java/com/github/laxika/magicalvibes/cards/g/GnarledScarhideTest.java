package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HourOfNeed;
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

@CardUsed({GnarledScarhide.class, GrizzlyBears.class, FountainOfYouth.class, HourOfNeed.class})
class GnarledScarhideTest extends BaseCardTest {

    @Test
    @DisplayName("Gnarled Scarhide can't block when cast as a creature")
    void creatureCantBlock() {
        Permanent scarhide = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        scarhide.setSummoningSick(false);
        addAttacker();

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Bestow gives the enchanted creature +2/+1 and prevents it from blocking")
    void bestowBoostsAndPreventsBlocking() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castWithAlternateCost(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(bls.canBlock(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Gnarled Scarhide cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }


    @Test
    @DisplayName("Normal casting needs no target and pays only the creature cost")
    void normalCastNeedsNoTarget() {
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gnarled Scarhide");
        Permanent scarhide = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, scarhide)).isTrue();
        assertThat(bls.canBlock(gd, scarhide)).isFalse();
    }

    @Test
    @DisplayName("Bestow can enchant an opponent's creature without changing its controller")
    void bestowCanEnchantOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(aura.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.isCreature(gd, aura)).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void illegalBestowTargetResolvesAsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.setHand(player2, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gnarled Scarhide");
        Permanent scarhide = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, scarhide)).isTrue();
        assertThat(scarhide.getAttachedTo()).isNull();
        assertThat(bls.canBlock(gd, scarhide)).isFalse();
        harness.assertNotInGraveyard(player1, "Gnarled Scarhide");
    }

    @Test
    @DisplayName("Bestowed Scarhide remains as a creature that can't block when its host leaves")
    void hostLeavingEndsBestow() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        harness.setHand(player1, List.of(new GnarledScarhide()));
        harness.setHand(player2, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent scarhide = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, scarhide)).isFalse();

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scarhide);
        assertThat(gqs.isCreature(gd, scarhide)).isTrue();
        assertThat(scarhide.getAttachedTo()).isNull();
        assertThat(bls.canBlock(gd, scarhide)).isFalse();
        harness.assertNotInGraveyard(player1, "Gnarled Scarhide");
    }

    private void addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

}
