package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EbonyOwlNetsuke;
import com.github.laxika.magicalvibes.cards.g.GodosIrregulars;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.StampedingSerow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurtainOfLight.class, EbonyOwlNetsuke.class, GodosIrregulars.class,
        PhantomWarrior.class, StampedingSerow.class})
class CurtainOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted unblocked attacker becomes blocked and deals no combat damage")
    void unblockedAttackerBecomesBlockedAndDealsNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));

        castCurtain(attacker.getId());

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The spell draws a card")
    void drawsCard() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.setLibrary(player2, List.of(new GodosIrregulars()));

        castCurtain(attacker.getId());

        harness.assertInHand(player2, "Godo's Irregulars");
    }

    @Test
    @DisplayName("Can be cast during the combat damage step")
    void castableDuringCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();

        castCurtain(attacker.getId());

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("Cannot be cast before blockers are declared")
    void cannotCastBeforeBlockers() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target an already blocked attacking creature")
    void cannotTargetBlockedAttacker() {
        Permanent blockedAttacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blockedAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an unblocked attacking creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        harness.addToBattlefield(player2, new EbonyOwlNetsuke());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();

        UUID netsukeId = harness.getPermanentId(player2, "Ebony Owl Netsuke");

        assertThatThrownBy(() -> harness.castInstant(player2, 0, netsukeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an unblocked attacking creature");
    }

    @Test
    @DisplayName("Works on a creature that cannot be blocked")
    void worksOnCreatureThatCannotBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new PhantomWarrior());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackersAndPrepareBlockers(List.of(0));

        castCurtain(attacker.getId());

        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new GodosIrregulars());
        Permanent nonAttacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an unblocked attacking creature");
    }

    @Test
    @DisplayName("Can be cast during the end of combat step")
    void castableDuringEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, List.of(new GodosIrregulars()));

        castCurtain(attacker.getId());

        harness.assertInHand(player2, "Godo's Irregulars");
    }

    @Test
    @DisplayName("Cannot be cast during the postcombat main phase")
    void cannotCastOutsideCombat() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An earlier Curtain does not draw when another Curtain has already blocked its target")
    void doesNotDrawWhenTargetBecomesBlockedBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new GodosIrregulars());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));
        harness.setLibrary(player2, List.of(new GodosIrregulars(), new EbonyOwlNetsuke()));
        harness.setHand(player2, List.of(new CurtainOfLight(), new CurtainOfLight()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, attacker.getId());
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Godo's Irregulars");
        harness.assertNotInHand(player2, "Ebony Owl Netsuke");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A trampling attacker still deals its full damage when blocked without blockers")
    void trampleStillDealsDamage() {
        Permanent attacker = addCreatureReady(player1, new StampedingSerow());
        addCreatureReady(player2, new GodosIrregulars());
        declareAttackers(List.of(0));

        castCurtain(attacker.getId());
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    private void castCurtain(UUID targetId) {
        giveSpell();
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    private void giveSpell() {
        harness.setHand(player2, List.of(new CurtainOfLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
