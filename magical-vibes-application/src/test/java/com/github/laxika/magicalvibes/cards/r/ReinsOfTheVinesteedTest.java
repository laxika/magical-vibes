package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReinsOfTheVinesteedTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature dies, Reins returns attached to the only shared-type creature and grants +2/+2")
    void returnsAttachedToSharedTypeCreature() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves());   // Elf (enchanted, will die)
        Permanent otherElf = addCreatureReady(player2, new ElvishWarrior());   // Elf — shares a type
        addCreatureReady(player1, new GrizzlyBears());                         // Bear — no shared type
        attachReinsTo(player1, dyingElf);

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player1, true); // accept — one valid target auto-attaches

        Permanent aura = findPermanent(player1, "Reins of the Vinesteed");
        assertThat(aura.getAttachedTo()).isEqualTo(otherElf.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Reins of the Vinesteed"));
        // +2/+2 applies to the new host (Elvish Warrior is 2/3)
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherElf)).isEqualTo(5);
    }

    @Test
    @DisplayName("Aura's controller chooses among multiple shared-type creatures")
    void controllerChoosesAmongMultipleSharedTypeCreatures() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves());   // Elf (enchanted, will die)
        Permanent elfA = addCreatureReady(player1, new ElvishWarrior());       // Elf
        Permanent elfB = addCreatureReady(player2, new LlanowarElves());       // Elf
        attachReinsTo(player1, dyingElf);

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player1, true); // accept — two valid targets, choose one
        harness.handlePermanentChosen(player1, elfB.getId());

        Permanent aura = findPermanent(player1, "Reins of the Vinesteed");
        assertThat(aura.getAttachedTo()).isEqualTo(elfB.getId());
        assertThat(aura.getAttachedTo()).isNotEqualTo(elfA.getId());
    }

    @Test
    @DisplayName("Declining the trigger leaves Reins in the graveyard")
    void decliningLeavesAuraInGraveyard() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new ElvishWarrior());
        attachReinsTo(player1, dyingElf);

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reins of the Vinesteed"));
        for (var bf : gd.playerBattlefields.values()) {
            assertThat(bf).noneMatch(p -> p.getCard().getName().equals("Reins of the Vinesteed"));
        }
    }

    @Test
    @DisplayName("Trigger fizzles when no creature shares a creature type with the dead creature")
    void fizzlesWhenNoSharedTypeCreatureExists() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves()); // Elf
        addCreatureReady(player1, new GrizzlyBears());                       // Bear — no shared type
        attachReinsTo(player1, dyingElf);

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player1, true); // accept, but nothing to attach to

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Reins of the Vinesteed"));
        for (var bf : gd.playerBattlefields.values()) {
            assertThat(bf).noneMatch(p -> p.getCard().getName().equals("Reins of the Vinesteed"));
        }
    }

    // ===== Helpers =====

    private Permanent attachReinsTo(Player auraController, Permanent creature) {
        Card aura = new ReinsOfTheVinesteed();
        Permanent auraPerm = new Permanent(aura);
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(auraPerm);
        return auraPerm;
    }

    private void destroyEnchantedCreature(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities(); // resolve Dark Banishing — creature dies, death trigger goes on stack
        harness.passBothPriorities(); // resolve death trigger — MayEffect prompts for the return
    }
}
