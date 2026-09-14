package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DartingMerfolk;
import com.github.laxika.magicalvibes.cards.s.SpectersWail;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.cards.v.Vendetta;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cowardice.class, DartingMerfolk.class, SpectersWail.class, StingingBarrier.class,
        Vendetta.class})
class CowardiceTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when a spell targets a creature")
    void triggersOnSpellTargetingCreature() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player1, "Darting Merfolk");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, merfolkId);

        // Vendetta + Cowardice triggered ability on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Resolving the trigger returns the targeted creature to its owner's hand")
    void resolvingReturnsCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player1, "Darting Merfolk");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, merfolkId);
        harness.passBothPriorities(); // resolve Cowardice trigger → bounce merfolk

        harness.assertNotOnBattlefield(player1, "Darting Merfolk");
        harness.assertInHand(player1, "Darting Merfolk");

        harness.passBothPriorities(); // Vendetta no longer has a legal target

        harness.assertInHand(player1, "Darting Merfolk");
        harness.assertInGraveyard(player2, "Vendetta");
    }

    @Test
    @DisplayName("Triggers on the controller's own spell targeting a creature")
    void triggersOnOwnSpell() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player1, "Darting Merfolk");

        harness.setHand(player1, List.of(new Vendetta()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, merfolkId);

        // Cowardice triggers regardless of who controls the spell
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Cowardice");
    }

    @Test
    @DisplayName("Triggers when an activated ability targets a creature")
    void triggersOnActivatedAbilityTargetingCreature() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player1, "Darting Merfolk");

        addCreatureReady(player2, new StingingBarrier());

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, merfolkId);

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

        harness.setHand(player2, List.of(new SpectersWail()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());

        // Only Specter's Wail on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Specter's Wail");
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

    @Test
    @DisplayName("Two Cowardices each trigger separately")
    void twoCowardicesStack() {
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new Cowardice());
        harness.addToBattlefield(player1, new DartingMerfolk());
        UUID merfolkId = harness.getPermanentId(player1, "Darting Merfolk");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Vendetta()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, merfolkId);

        // Vendetta + 2 Cowardice triggers on the stack
        assertThat(gd.stack).hasSize(3);
    }
}
