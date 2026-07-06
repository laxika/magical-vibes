package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BrazenBuccaneers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowedCaravelTest extends BaseCardTest {

    // ===== Explore triggers — land on top =====

    @Test
    @DisplayName("Explore with land puts a +1/+1 counter on Shadowed Caravel")
    void exploreLandPutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        castExplorerAndResolveExplore();

        // Explore trigger resolves automatically (no target needed for PutCountersOnSelfEffect)
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    // ===== Explore triggers — non-land on top =====

    @Test
    @DisplayName("Explore with non-land (accept graveyard) puts a +1/+1 counter on Shadowed Caravel")
    void exploreNonLandAcceptPutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Explore with non-land (decline, keep on top) puts a +1/+1 counter on Shadowed Caravel")
    void exploreNonLandDeclinePutsCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        castExplorerAndResolveExplore();

        // May ability for explore graveyard choice
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    // ===== Multiple explores =====

    @Test
    @DisplayName("Multiple explores accumulate +1/+1 counters")
    void multipleExploresAccumulateCounters() {
        Permanent caravel = addCaravelReady(player1);

        // First explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Second explore (land)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    // ===== Explore with empty library — no trigger =====

    @Test
    @DisplayName("Explore with empty library does not put a counter")
    void exploreEmptyLibraryNoCounter() {
        Permanent caravel = addCaravelReady(player1);

        gd.playerDecks.get(player1.getId()).clear();

        castExplorerAndResolveExplore();

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Crew mechanic =====

    @Test
    @DisplayName("Caravel is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent caravel = addCaravelReady(player1);

        assertThat(gqs.isCreature(gd, caravel)).isFalse();
        assertThat(caravel.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 2 animates Caravel")
    void crewWithSufficientPower() {
        Permanent caravel = addCaravelReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(caravel.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, caravel)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters boost effective power/toughness when crewed")
    void countersBoostedWhenCrewedAndAnimated() {
        Permanent caravel = addCaravelReady(player1);

        // Get a +1/+1 counter from explore
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        castExplorerAndResolveExplore();
        harness.passBothPriorities(); // resolve counter trigger

        assertThat(caravel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Crew the Caravel
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Base 2/2 + one +1/+1 counter = 3/3
        assertThat(gqs.getEffectivePower(gd, caravel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caravel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addCaravelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    // ===== Helpers =====

    private Permanent addCaravelReady(Player player) {
        Permanent perm = new Permanent(new ShadowedCaravel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castExplorerAndResolveExplore() {
        harness.setHand(player1, List.of(new BrazenBuccaneers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB explore trigger
    }
}
