package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.a.AvenWarcraft;
import com.github.laxika.magicalvibes.cards.c.CephalidInkshrouder;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({InfectiousRage.class, ToxicStench.class, AnuridBarkripper.class,
        AvenWarcraft.class, CephalidInkshrouder.class})
class InfectiousRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/-1")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new AnuridBarkripper());
        attachRageTo(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage attaches to the only legal creature, including a shrouded creature")
    void returnsToOnlyLegalCreatureWithoutTargeting() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
        Permanent shroudedCreature = addShroudedCreature();
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(aura.getAttachedTo()).isEqualTo(shroudedCreature.getId());
    }

    @Test
    @DisplayName("When enchanted creature dies, Infectious Rage chooses randomly among legal creatures")
    void choosesAmongLegalCreatures() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
        Permanent firstCandidate = addCreatureReady(player1, new AnuridBarkripper());
        Permanent secondCandidate = addCreatureReady(player2, new AnuridBarkripper());
        attachRageTo(player1, dyingCreature);

        destroyCreature(dyingCreature);

        Permanent aura = findPermanent(player1, "Infectious Rage");
        assertThat(List.of(firstCandidate.getId(), secondCandidate.getId())).contains(aura.getAttachedTo());
    }

    @Test
    @DisplayName("When enchanted creature dies with no legal creature, Infectious Rage stays in the graveyard")
    void staysInGraveyardWithoutLegalCreature() {
        Permanent dyingCreature = addCreatureReady(player1, new AnuridBarkripper());
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

    private Permanent addShroudedCreature() {
        Permanent creature = addCreatureReady(player2, new CephalidInkshrouder());

        harness.setHand(player2, List.of(new AvenWarcraft()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0);

        harness.setHand(player2, List.of(new ToxicStench()));
        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        harness.activateAbility(player2, creatureIndex, null, null);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        return creature;
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ToxicStench()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();
    }
}
