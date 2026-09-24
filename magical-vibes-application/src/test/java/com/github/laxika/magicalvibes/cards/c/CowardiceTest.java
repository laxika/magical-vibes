package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpectersWail;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cowardice.class, Confiscate.class, GrizzlyBears.class, Naturalize.class, Forest.class, NaturalAffinity.class, SamiteHealer.class, Shock.class, DartingMerfolk.class, SpectersWail.class, StingingBarrier.class, Vendetta.class})
class CowardiceTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when a spell targets a creature")
    void triggersOnSpellTargetingCreature() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + Cowardice triggered ability on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Resolving the trigger returns the targeted creature to its owner's hand")
    void resolvingReturnsCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // resolve Cowardice trigger → bounce bears

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers on the controller's own spell targeting a creature")
    void triggersOnOwnSpell() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);

        // Cowardice triggers regardless of who controls the spell
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Triggers when an activated ability targets a creature")
    void triggersOnActivatedAbilityTargetingCreature() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        addCreatureReady(player2, new SamiteHealer());

        harness.activateAbility(player2, 0, null, bearsId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Does NOT trigger when a spell targets a player")
    void doesNotTriggerOnPlayerTarget() {
        harness.addToBattlefield(player1, new Cowardice());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        // Only the Shock spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Does NOT trigger when a noncreature permanent is targeted")
    void doesNotTriggerOnNonCreaturePermanentTarget() {
        harness.addToBattlefield(player1, new Cowardice());
        UUID cowardiceId = harness.getPermanentId(player1, "Cowardice");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, cowardiceId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Naturalize");
    }

    @Test
    @DisplayName("Triggers when an animated land is targeted")
    void triggersWhenAnimatedLandIsTargeted() {
        harness.addToBattlefield(player1, new Cowardice());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, forest.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Returns an opponent-controlled creature to its owner's hand")
    void returnsOpponentControlledCreatureToOwnersHand() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Confiscate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));

        harness.addToBattlefield(player1, new Cowardice());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two Cowardices each trigger separately")
    void twoCowardicesStack() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bearsId);

        // Shock + 2 Cowardice triggers on the stack
        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Triggers when an opponent's creature becomes the target")
    void triggersForOpponentCreature() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player2, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player2, "Darting Merfolk");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, merfolkId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Returns a controlled creature to its owner's hand")
    void returnsControlledCreatureToOwnersHand() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new DartingMerfolk());
        gd.playerBattlefields.get(player1.getId()).remove(merfolk);
        gd.playerBattlefields.get(player2.getId()).add(merfolk);
        gd.stolenCreatures.put(merfolk.getId(), player1.getId());
        harness.addToBattlefield(player2, new Cowardice());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, merfolk.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Darting Merfolk");
        harness.assertInHand(player1, "Darting Merfolk");
        harness.assertNotInHand(player2, "Darting Merfolk");
    }
}
