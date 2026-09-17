package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuinRat;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshcoatOfTheShadowSwarm.class, BogRats.class, Forest.class, GrizzlyBears.class,
        RuinRat.class, Shock.class})
class AshcoatOfTheShadowSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other Rats by the number of Rats controlled")
    void attackingBoostsOtherRats() {
        Permanent ashcoat = addCreatureReady(player1, new AshcoatOfTheShadowSwarm());
        Permanent ownRat = addCreatureReady(player1, new BogRats());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingRat = addCreatureReady(player2, new BogRats());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(ownRat.getEffectivePower()).isEqualTo(3);
        assertThat(ownRat.getEffectiveToughness()).isEqualTo(3);
        assertThat(ashcoat.getEffectivePower()).isEqualTo(3);
        assertThat(ashcoat.getEffectiveToughness()).isEqualTo(4);
        assertThat(ownBear.getEffectivePower()).isEqualTo(2);
        assertThat(ownBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingRat.getEffectivePower()).isEqualTo(1);
        assertThat(opposingRat.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking boosts other Rats by the number of Rats controlled")
    void blockingBoostsOtherRats() {
        Permanent ashcoat = addCreatureReady(player1, new AshcoatOfTheShadowSwarm());
        Permanent ownRat = addCreatureReady(player1, new BogRats());
        addCreatureReady(player2, new GrizzlyBears()).setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(ownRat.getEffectivePower()).isEqualTo(3);
        assertThat(ownRat.getEffectiveToughness()).isEqualTo(3);
        assertThat(ashcoat.getEffectivePower()).isEqualTo(3);
        assertThat(ashcoat.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Accepting the end-step ability mills four and returns up to two Rat creatures")
    void acceptsEndStepAbilityAndReturnsRats() {
        addCreatureReady(player1, new AshcoatOfTheShadowSwarm());
        Card firstRat = new BogRats();
        Card secondRat = new RuinRat();
        Card nonRat = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstRat, secondRat, nonRat));
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new Forest(), new Shock()));

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices().stream()
                .map(index -> gd.playerGraveyards.get(player1.getId()).get(index).getId())
                .toList()).containsExactlyInAnyOrder(firstRat.getId(), secondRat.getId());

        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(firstRat));
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(secondRat));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstRat, secondRat);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonRat);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the end-step ability does not mill or return cards")
    void declinesEndStepAbility() {
        addCreatureReady(player1, new AshcoatOfTheShadowSwarm());
        Card rat = new BogRats();
        harness.setGraveyard(player1, List.of(rat));
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new Forest(), new Shock()));

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rat);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(rat);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
