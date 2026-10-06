package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
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

@CardUsed({SinisterPossession.class, GrizzlyBears.class, FountainOfYouth.class, PalaceGuard.class})
class SinisterPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature attacking makes its controller lose 2 life")
    void attackingLosesTwoLife() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachPossession(bears);

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Enchanted creature blocking makes its controller lose 2 life")
    void blockingLosesTwoLife() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachPossession(blocker);

        int lifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An unenchanted creature attacking costs no life")
    void unenchantedAttackerLosesNoLife() {
        addCreatureReady(player1, new GrizzlyBears());

        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SinisterPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting the Aura on an opposing creature attaches it and penalizes its controller")
    void castOnOpposingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinisterPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Sinister Possession").getAttachedTo()).isEqualTo(creature.getId());
        int lifeBefore = gd.getLife(player2.getId());
        int auraControllerLife = gd.getLife(player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(auraControllerLife);
    }

    @Test
    @DisplayName("Multiple copies each trigger when the enchanted creature attacks")
    void multipleCopiesTriggerIndependently() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachPossession(creature);
        attachPossession(creature);
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Removing the Aura does not stop an already triggered ability")
    void triggerSurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachPossession(creature);
        int lifeBefore = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(aura);
            gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Life loss uses the enchanted creature's controller when the trigger resolves")
    void controllerChangesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachPossession(creature);
        int originalControllerLife = gd.getLife(player1.getId());
        int newControllerLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(creature);
            gd.playerBattlefields.get(player2.getId()).add(creature);
            creature.setAttacking(false);
            resolveAllTriggers();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(originalControllerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(newControllerLife - 2);
    }

    @Test
    @DisplayName("Blocking multiple attackers triggers life loss only once")
    void blockingMultipleCreaturesLosesOnlyTwoLife() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new PalaceGuard());
        attachPossession(blocker);
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private Permanent attachPossession(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinisterPossession());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
