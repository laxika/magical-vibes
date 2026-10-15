package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeovoldEmissaryOfTrest.class, Forest.class, Island.class, GrizzlyBears.class,
        Shock.class, ProdigalPyromancer.class, TurnToFrog.class})
class LeovoldEmissaryOfTrestTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can draw only one card each turn")
    void limitsOpponentDrawsPerTurn() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
        assertThat(gd.cardsDrawnThisTurn.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not restrict the controller's draws")
    void doesNotRestrictControllerDraws() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May draw when an opponent targets the controller")
    void drawsWhenOpponentTargetsController() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        castOpponentShock(player1.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("May draw when an opponent targets a permanent")
    void drawsWhenOpponentTargetsPermanent() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        castOpponentShock(bearsId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when the controller targets their own permanent")
    void doesNotTriggerOnControllersSpell() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Targeting Leovold himself also triggers the optional draw")
    void drawsWhenOpponentTargetsLeovold() {
        UUID leovoldId = harness.addToBattlefieldAndReturn(player1, new LeovoldEmissaryOfTrest()).getId();
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        castOpponentShock(leovoldId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The controller may decline the draw")
    void mayDeclineDraw() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        castOpponentShock(player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("An opponent's activated ability targeting the controller triggers a draw")
    void drawsWhenOpponentAbilityTargetsController() {
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());
        addCreatureReady(player2, new ProdigalPyromancer());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Cards drawn before Leovold enters count toward the turn's limit")
    void countsDrawsBeforeEnteringBattlefield() {
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.addToBattlefield(player1, new LeovoldEmissaryOfTrest());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Losing all abilities removes Leovold's draw restriction")
    void losingAbilitiesRemovesDrawRestriction() {
        UUID leovoldId = harness.addToBattlefieldAndReturn(player1, new LeovoldEmissaryOfTrest()).getId();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, leovoldId);
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gd.playerHands.get(player2.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities removes Leovold's targeting trigger")
    void losingAbilitiesRemovesTargetingTrigger() {
        UUID leovoldId = harness.addToBattlefieldAndReturn(player1, new LeovoldEmissaryOfTrest()).getId();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, leovoldId);

        castOpponentShock(player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void castOpponentShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
    }
}
