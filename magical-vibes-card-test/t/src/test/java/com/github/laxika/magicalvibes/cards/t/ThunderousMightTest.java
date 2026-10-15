package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderousMight.class, SwordwiseCentaur.class, NyxbornRollicker.class, AuraGraft.class})
class ThunderousMightTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking enchanted creature gets +X/+0 for red devotion")
    void attackingEnchantedCreatureGetsRedDevotionBoost() {
        Permanent creature = addCreatureReady(player1, new SwordwiseCentaur());
        addCreatureReady(player1, new NyxbornRollicker());
        castOnCreature(creature);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new SwordwiseCentaur());
        castOnCreature(creature);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's enchanted attacker uses the Aura controller's devotion")
    void opposingAttackerUsesAuraControllersDevotion() {
        Permanent creature = addCreatureReady(player2, new SwordwiseCentaur());
        addCreatureReady(player2, new NyxbornRollicker());
        addCreatureReady(player2, new NyxbornRollicker());
        castOnCreature(creature);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Devotion is counted when the attack trigger resolves")
    void devotionIsCountedAtResolution() {
        Permanent creature = addCreatureReady(player1, new SwordwiseCentaur());
        castOnCreature(creature);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new NyxbornRollicker());
        resolveAllTriggers();
        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.addToBattlefield(player1, new NyxbornRollicker());
        assertThat(creature.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Another creature attacking does not boost the enchanted creature")
    void anotherCreatureAttackingDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new SwordwiseCentaur());
        addCreatureReady(player1, new NyxbornRollicker());
        castOnCreature(creature);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Moving the Aura in response still boosts the creature that attacked")
    void movingAuraDoesNotChangeWhichCreatureGetsBoosted() {
        Permanent attacker = addCreatureReady(player1, new SwordwiseCentaur());
        Permanent otherCreature = addCreatureReady(player1, new SwordwiseCentaur());
        castOnCreature(attacker);
        Permanent aura = findPermanent(player1, "Thunderous Might");
        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, otherCreature.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(otherCreature.getId());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isZero();
    }

    private void castOnCreature(Permanent creature) {
        harness.setHand(player1, List.of(new ThunderousMight()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
