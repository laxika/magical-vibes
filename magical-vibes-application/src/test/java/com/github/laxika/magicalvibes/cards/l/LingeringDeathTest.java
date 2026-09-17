package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.cards.z.ZurgoThundersDecree;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LingeringDeath.class, GoblinBrigand.class})
class LingeringDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the enchanted creature at the beginning of its controller's end step")
    void sacrificesAtEnchantedControllerEndStep() {
        Permanent creature = attachToOpponentCreature();

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player2, "Goblin Brigand");
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's end step")
    void doesNotTriggerDuringAuraControllerEndStep() {
        Permanent creature = attachToOpponentCreature();

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Goblin Brigand");
    }

    @Test
    @CardUsed({Stabilizer.class})
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Stabilizer());

        harness.setHand(player1, List.of(new LingeringDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Uses the original enchanted creature if the Aura leaves before resolution")
    void triggerUsesLastKnownAttachmentWhenAuraLeaves() {
        Permanent creature = attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Lingering Death");

        beginEndStep(player2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player2, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Lingering Death");
    }

    @Test
    @CardUsed({RayOfCommand.class})
    @DisplayName("Does not sacrifice the creature after it changes control before resolution")
    void doesNotSacrificeCreatureThatChangedControl() {
        Permanent creature = attachToOpponentCreature();

        beginEndStep(player2);
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @CardUsed({SigardaHostOfHerons.class})
    @DisplayName("An opponent-controlled Aura cannot cause Sigarda's controller to sacrifice")
    void opponentAuraCannotCauseSacrificeThroughSigarda() {
        Permanent sigarda = addCreatureReady(player2, new SigardaHostOfHerons());
        Permanent aura = new Permanent(new LingeringDeath());
        aura.setAttachedTo(sigarda.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sigarda);
        harness.assertNotInGraveyard(player2, "Sigarda, Host of Herons");
    }

    @Test
    @CardUsed({Bloodbriar.class})
    @DisplayName("Sacrificing the enchanted creature triggers its controller's sacrifice abilities")
    void sacrificeTriggersAllyPermanentSacrificedAbilities() {
        Permanent bloodbriar = addCreatureReady(player2, new Bloodbriar());
        attachToOpponentCreature();

        runEndStep(player2);
        resolveAllTriggers();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @CardUsed({ZurgoThundersDecree.class})
    @DisplayName("Does not sacrifice a creature protected from sacrifice")
    void respectsCantBeSacrificedEffect() {
        addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent warrior = findPermanents(player1, "Warrior").getFirst();

        Permanent aura = new Permanent(new LingeringDeath());
        aura.setAttachedTo(warrior.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warrior);
    }

    private Permanent attachToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GoblinBrigand());

        harness.setHand(player1, List.of(new LingeringDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private void runEndStep(Player player) {
        beginEndStep(player);
        harness.passBothPriorities();
    }

    private void beginEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
    }
}
