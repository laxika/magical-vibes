package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({Confiscate.class, ArgothianSwine.class, Forest.class, Disenchant.class, Expunge.class})
class ConfiscateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Confiscate targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Resolving Confiscate steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Confiscate
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));

        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Confiscate");
    }

    @Test
    @DisplayName("Permanent returns to owner when Confiscate is destroyed")
    void permanentReturnsWhenConfiscateDestroyed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent confiscatePerm = findPermanent(player1, "Confiscate");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, confiscatePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Confiscate leaves the battlefield when the enchanted creature dies")
    void auraLeavesWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Expunge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Argothian Swine");
        harness.assertInGraveyard(player1, "Confiscate");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }
}
