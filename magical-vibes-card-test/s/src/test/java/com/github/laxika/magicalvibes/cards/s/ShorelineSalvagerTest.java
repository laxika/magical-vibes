package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShorelineSalvager.class, Island.class, Forest.class, GrizzlyBears.class})
class ShorelineSalvagerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage with an Island offers a card draw")
    void combatDamageWithIslandOffersDraw() {
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        attackWithSalvagerDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Declining the draw leaves the hand unchanged")
    void mayDrawCanBeDeclined() {
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        attackWithSalvagerDealingDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage without a controlled Island does not trigger")
    void noTriggerWithoutControlledIsland() {
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        attackWithSalvagerDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Losing the last Island before resolution prevents the draw")
    void islandConditionIsRecheckedAtResolution() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = findPermanent(player1, "Island");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        dealCombatDamageWithoutResolvingTrigger();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerHands.get(player1.getId()).add(island.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An Island entering after damage cannot create a missed trigger")
    void gainingIslandAfterDamageDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        dealCombatDamageWithoutResolvingTrigger();
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new Island());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw trigger survives Shoreline Salvager leaving the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        dealCombatDamageWithoutResolvingTrigger();
        assertThat(gd.stack).hasSize(1);
        Permanent salvager = findPermanent(player1, "Shoreline Salvager");
        gd.playerBattlefields.get(player1.getId()).remove(salvager);
        gd.playerGraveyards.get(player1.getId()).add(salvager.getCard());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not trigger a draw")
    void damageToCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        Permanent salvager = addCreatureReady(player1, new ShorelineSalvager());
        salvager.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void dealCombatDamageWithoutResolvingTrigger() {
        Permanent salvager = addCreatureReady(player1, new ShorelineSalvager());
        salvager.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player2, 20);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 17);
    }

    private void attackWithSalvagerDealingDamage() {
        Permanent salvager = addCreatureReady(player1, new ShorelineSalvager());
        salvager.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
