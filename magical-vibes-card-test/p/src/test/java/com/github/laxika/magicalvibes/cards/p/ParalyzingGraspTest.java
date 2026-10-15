package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.s.SavageSurge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParalyzingGrasp.class, DrudgeBeetle.class, SavageSurge.class})
class ParalyzingGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Paralyzing Grasp attaches to the targeted creature and does not tap it")
    void resolvingAttachesWithoutTapping() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());

        harness.setHand(player1, List.of(new ParalyzingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        Permanent grasp = findPermanent(player1, "Paralyzing Grasp");
        assertThat(grasp.isAttached()).isTrue();
        assertThat(grasp.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        creature.tap();

        attachGrasp(creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchanted = addCreatureReady(player2, new DrudgeBeetle());
        enchanted.tap();
        Permanent free = addCreatureReady(player2, new DrudgeBeetle());
        free.tap();

        attachGrasp(enchanted);

        advanceToUpkeep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Paralyzing Grasp leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        creature.tap();

        Permanent grasp = attachGrasp(creature);
        gd.playerBattlefields.get(player1.getId()).remove(grasp);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paralyzing Grasp fizzles if the target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());

        harness.setHand(player1, List.of(new ParalyzingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Paralyzing Grasp");
        harness.assertNotOnBattlefield(player1, "Paralyzing Grasp");
    }

    private Permanent attachGrasp(Permanent creature) {
        Permanent grasp = new Permanent(new ParalyzingGrasp());
        grasp.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(grasp);
        return grasp;
    }

    @Test
    @DisplayName("Paralyzing Grasp can enchant its controller's creature and prevents repeated untaps")
    void ownCreatureRemainsTappedAcrossUntapSteps() {
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        creature.tap();
        harness.setHand(player1, List.of(new ParalyzingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();
        advanceToUpkeep(player2);
        advanceToUpkeep(player1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Paralyzing Grasp").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An untap spell can untap the enchanted creature without removing Paralyzing Grasp")
    void untapSpellStillWorks() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        creature.tap();
        Permanent grasp = attachGrasp(creature);
        harness.setHand(player1, List.of(new SavageSurge()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(grasp.getAttachedTo()).isEqualTo(creature.getId());

        creature.tap();
        advanceToUpkeep(player2);
        assertThat(creature.isTapped()).isTrue();
    }
}
