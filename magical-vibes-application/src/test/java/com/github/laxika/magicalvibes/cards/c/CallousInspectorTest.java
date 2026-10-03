package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallousInspector.class, Shock.class})
class CallousInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Callous Inspector dies, it damages its controller and creates a Clue")
    void deathTriggerDamagesControllerAndCreatesClue() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CallousInspector());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, inspector.getId());
        harness.assertInGraveyard(player1, "Callous Inspector");

        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Damage and Clue creation resolve together as one death ability")
    void deathAbilityResolvesAsOneTrigger() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CallousInspector());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, inspector.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Clue");

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Clue can be sacrificed for two mana to draw a card")
    void createdClueCanDrawACard() {
        Permanent inspector = harness.addToBattlefieldAndReturn(player1, new CallousInspector());
        CallousInspector drawnCard = new CallousInspector();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, inspector.getId());
        resolveAllTriggers();

        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, clueIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Menace rejects one blocker but permits two blockers")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new CallousInspector());
        Permanent firstBlocker = addCreatureReady(player2, new CallousInspector());
        Permanent secondBlocker = addCreatureReady(player2, new CallousInspector());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
