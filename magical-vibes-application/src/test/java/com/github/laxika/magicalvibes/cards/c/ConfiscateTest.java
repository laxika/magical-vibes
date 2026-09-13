package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Confiscate.class, Disenchant.class, Forest.class, GrizzlyBears.class})
class ConfiscateTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Confiscate targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Confiscate steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Confiscate")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));

        assertThat(gd.stolenCreatures).containsEntry(bears.getId(), player2.getId());
    }

    @Test
    @DisplayName("Resolving Confiscate steals a noncreature permanent (a land)")
    void resolvingStealsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.stolenCreatures).containsEntry(forest.getId(), player2.getId());
    }

    @Test
    @DisplayName("Confiscate fizzles if target is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Confiscate");
    }

    @Test
    @DisplayName("Permanent returns to owner when Confiscate is destroyed")
    void permanentReturnsWhenConfiscateDestroyed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent confiscatePerm = findPermanent(player1, "Confiscate");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, confiscatePerm.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(bears.getId());
    }
}
