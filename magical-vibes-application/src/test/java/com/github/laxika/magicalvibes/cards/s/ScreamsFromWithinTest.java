package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreamsFromWithinTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void givesEnchantedCreatureMinusOneMinusOne() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachScreamsTo(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature dies, Screams returns attached to the only legal creature")
    void returnsAttachedToOnlyLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        attachScreamsTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Screams from Within");
        assertThat(aura.getAttachedTo()).isEqualTo(targetCreature.getId());
        harness.assertNotInGraveyard(player1, "Screams from Within");
    }

    @Test
    @DisplayName("Controller chooses the creature for Screams to enchant")
    void controllerChoosesAmongLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosenTarget = addCreatureReady(player2, new GrizzlyBears());
        attachScreamsTo(player1, dyingCreature);

        destroyCreature(dyingCreature);
        harness.handlePermanentChosen(player1, chosenTarget.getId());

        Permanent aura = findPermanent(player1, "Screams from Within");
        assertThat(aura.getAttachedTo()).isEqualTo(chosenTarget.getId());
        assertThat(aura.getAttachedTo()).isNotEqualTo(firstTarget.getId());
    }

    @Test
    @DisplayName("Return trigger fizzles when there is no legal creature")
    void remainsInGraveyardWithoutLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        attachScreamsTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        harness.assertInGraveyard(player1, "Screams from Within");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Screams from Within"));
    }

    private Permanent attachScreamsTo(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new ScreamsFromWithin());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
