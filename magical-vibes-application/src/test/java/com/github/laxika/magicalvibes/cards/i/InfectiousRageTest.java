package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JolraelsCentaur;
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

@CardUsed({InfectiousRage.class, DarkBanishing.class, GrizzlyBears.class, JolraelsCentaur.class})
class InfectiousRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/-1")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachRageTo(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage attaches to the only legal creature, including a shrouded creature")
    void returnsToOnlyLegalCreatureWithoutTargeting() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shroudedCreature = addCreatureReady(player2, new JolraelsCentaur());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(shroudedCreature.getId());
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage chooses randomly among legal creatures")
    void choosesAmongLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstCandidate = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCandidate = addCreatureReady(player2, new GrizzlyBears());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(List.of(firstCandidate.getId(), secondCandidate.getId())).contains(aura.getAttachedTo());
    }

    @Test
    @DisplayName("When enchanted creature dies with no legal creature, Infectious Rage stays in the graveyard")
    void staysInGraveyardWithoutLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new GrizzlyBears());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        harness.assertInGraveyard(player1, "Infectious Rage");
        harness.assertNotOnBattlefield(player1, "Infectious Rage");
    }

    private Permanent attachRageTo(Player controller, Permanent creature) {
        Card aura = new InfectiousRage();
        Permanent auraPermanent = new Permanent(aura);
        auraPermanent.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(auraPermanent);
        return auraPermanent;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
