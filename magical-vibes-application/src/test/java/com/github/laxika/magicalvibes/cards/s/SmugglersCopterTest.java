package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmugglersCopterTest extends BaseCardTest {

    @Test
    void attackingOffersOptionalDrawThenDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        crew(copter);

        declareAttackers(player1, List.of(0));
        resolveLootChoice();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void blockingOffersOptionalDrawThenDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        crew(copter);

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveLootChoice();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningLootDoesNotDrawOrDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        crew(copter);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void crewAnimatesCopterAndTapsCreature() {
        Permanent copter = addCopterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        crew(copter);

        assertThat(gqs.isCreature(gd, copter)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    private void setUpLootCards() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
    }

    private Permanent addCopterReady(Player player) {
        Permanent copter = new Permanent(new SmugglersCopter());
        copter.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(copter);
        return copter;
    }

    private void crew(Permanent copter) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copter), null, null);
        harness.passBothPriorities();
    }

    private void resolveLootChoice() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
    }
}
