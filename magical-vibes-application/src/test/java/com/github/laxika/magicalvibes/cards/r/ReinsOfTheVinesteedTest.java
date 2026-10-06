package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntingTriad;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Progenitus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReinsOfTheVinesteed.class, LlanowarElves.class, ElvishWarrior.class, GrizzlyBears.class,
        DarkBanishing.class, ChangelingSentinel.class, Cremate.class, HuntingTriad.class, Progenitus.class})
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
        harness.assertNotInGraveyard(player1, "Reins of the Vinesteed");
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

        harness.assertInGraveyard(player1, "Reins of the Vinesteed");
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

        harness.assertInGraveyard(player1, "Reins of the Vinesteed");
        for (var bf : gd.playerBattlefields.values()) {
            assertThat(bf).noneMatch(p -> p.getCard().getName().equals("Reins of the Vinesteed"));
        }
    }

    @Test
    void castingAuraBoostsEnchantedCreature() {
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ReinsOfTheVinesteed()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reins of the Vinesteed").getAttachedTo()).isEqualTo(elf.getId());
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(5);
    }

    @Test
    void returnsAfterEnchantedTokenCeasesToExist() {
        harness.setHand(player1, List.of(new HuntingTriad()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        List<Permanent> elves = findPermanents(player1, "Elf Warrior");
        Permanent dyingElf = elves.getFirst();
        Permanent survivingElf = elves.get(1);
        attachReinsTo(player1, dyingElf);

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, survivingElf.getId());

        assertThat(findPermanent(player1, "Reins of the Vinesteed").getAttachedTo())
                .isEqualTo(survivingElf.getId());
    }

    @Test
    void returnsEvenIfDeadCreatureIsExiledBeforeTriggerResolves() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves());
        Permanent otherElf = addCreatureReady(player1, new ElvishWarrior());
        attachReinsTo(player1, dyingElf);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing(), new Cremate()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, dyingElf.getId());
        harness.castAndResolveInstant(player2, 0, dyingElf.getCard().getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Reins of the Vinesteed").getAttachedTo()).isEqualTo(otherElf.getId());
    }

    @Test
    void cannotReturnAuraFromAnotherPlayersGraveyard() {
        Permanent dyingElf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent aura = attachReinsTo(player1, dyingElf);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).add(aura);
        gd.stolenCreatures.put(aura.getId(), player1.getId());

        destroyEnchantedCreature(dyingElf);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Reins of the Vinesteed");
        harness.assertNotOnBattlefield(player2, "Reins of the Vinesteed");
    }

    @Test
    void ignoresSharedTypeCreatureThatCannotBeEnchanted() {
        Permanent dyingChangeling = addCreatureReady(player1, new ChangelingSentinel());
        Permanent legalElf = addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player2, new Progenitus());
        attachReinsTo(player1, dyingChangeling);

        destroyEnchantedCreature(dyingChangeling);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Reins of the Vinesteed").getAttachedTo()).isEqualTo(legalElf.getId());
    }

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
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }
}
