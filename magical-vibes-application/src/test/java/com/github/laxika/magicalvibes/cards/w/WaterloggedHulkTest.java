package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterloggedHulk.class, WatertightGondola.class, GrizzlyBears.class, Island.class})
class WaterloggedHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Waterlogged Hulk mills a card")
    void tapsToMill() {
        Card milled = new GrizzlyBears();
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new WaterloggedHulk());
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled);
        assertThat(hulk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Craft exiles an Island and returns Watertight Gondola transformed")
    void craftsWithIslandFromBattlefield() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new WaterloggedHulk());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hulk, island);
        assertThat(gd.findExiledCard(island.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof WatertightGondola);
    }

    @Test
    @DisplayName("Craft accepts an Island card from the graveyard")
    void craftsWithIslandFromGraveyard() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new WaterloggedHulk());
        Island island = new Island();
        harness.setGraveyard(player1, List.of(island));
        addCraftMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(island.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof WatertightGondola);
    }

    @Test
    @DisplayName("Watertight Gondola cannot be blocked with eight permanent cards in its graveyard")
    void cannotBeBlockedWithDescendEight() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Island(), new Island(), new Island(), new Island()));
        Permanent gondola = addReady(player1, new WatertightGondola());
        addReady(player1, new GrizzlyBears());
        Permanent blocker = addReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gondola.setAttacking(true);

        assertThat(gqs.isCreature(gd, gondola)).isTrue();
        assertThatThrownBy(() -> declareBlock(blocker, gondola))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Crew 1 animates Watertight Gondola")
    void crewAnimatesGondola() {
        Permanent gondola = addReady(player1, new WatertightGondola());
        Permanent creature = addReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gondola)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
