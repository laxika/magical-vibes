package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowerFlourish.class, Forest.class, Plains.class, Island.class, GrizzlyBears.class,
        TempleGarden.class})
class FlowerFlourishTest extends BaseCardTest {

    @Test
    @DisplayName("Flower searches for a basic Forest or Plains card")
    void flowerSearchesForForestOrPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, plains, island, bears));
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, search.params().cards().indexOf(forest));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, island, bears);
    }

    @Test
    @DisplayName("Flourish pumps only creatures the caster controls until end of turn")
    void flourishPumpsOwnCreaturesUntilEndOfTurn() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(ownBears.getEffectivePower()).isEqualTo(4);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingBears.getEffectivePower()).isEqualTo(2);
        assertThat(opposingBears.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.getEffectivePower()).isEqualTo(2);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void flowerCanBePaidForWithWhiteManaAndFindPlains() {
        Plains plains = new Plains();
        TempleGarden garden = new TempleGarden();
        harness.setLibrary(player1, List.of(plains, garden));
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(garden);
    }

    @Test
    void flowerMayFailToFindEvenWhenEligibleLandExists() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void flowerResolvesWithNoEligibleLand() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flourishAffectsCreaturesPresentAtResolutionButNotLaterEntrants() {
        Permanent beforeCast = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlowerFlourish()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(beforeCast.getEffectivePower()).isEqualTo(4);
        assertThat(beforeCast.getEffectiveToughness()).isEqualTo(4);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(4);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }
}
