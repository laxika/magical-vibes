package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Colossadactyl;
import com.github.laxika.magicalvibes.cards.e.EarthshakerDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenNecropolis.class, Forest.class, GrizzlyBears.class,
        Colossadactyl.class, EarthshakerDreadmaw.class})
class HiddenNecropolisTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new HiddenNecropolis()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void tapsForBlackMana() {
        Permanent necropolis = addReadyNecropolis();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(necropolis.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDiscoversFour() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));
        addReadyNecropolis();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Hidden Necropolis");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyNecropolis() {
        return harness.addToBattlefieldAndReturn(player1, new HiddenNecropolis());
    }

    @Test
    void castsManaValueFourWithoutPayingAndBottomsSkippedCards() {
        Forest skippedLand = new Forest();
        EarthshakerDreadmaw tooExpensive = new EarthshakerDreadmaw();
        Colossadactyl discovered = new Colossadactyl();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, tooExpensive, discovered, untouched));
        addReadyNecropolis();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Hidden Necropolis");
        harness.assertInGraveyard(player1, "Hidden Necropolis");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(skippedLand, tooExpensive, discovered, untouched);

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourceZone()).isEqualTo(Zone.EXILE);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, tooExpensive);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Colossadactyl");
    }

    @Test
    void returnsAllCardsWhenLibraryHasNoQualifyingCard() {
        Forest land = new Forest();
        EarthshakerDreadmaw expensive = new EarthshakerDreadmaw();
        harness.setLibrary(player1, List.of(land, expensive));
        addReadyNecropolis();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, expensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hidden Necropolis");
    }

    @Test
    void discoversWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        addReadyNecropolis();
        addDiscoverMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hidden Necropolis");
    }

    @Test
    void cannotDiscoverOutsideMainPhase() {
        Permanent necropolis = addReadyNecropolis();
        addDiscoverMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(necropolis.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hidden Necropolis");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDiscoverOnOpponentsTurn() {
        addReadyNecropolis();
        addDiscoverMana();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hidden Necropolis");
    }

    @Test
    void cannotUseManaAbilityThenDiscoverFromSameTappedLand() {
        addReadyNecropolis();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hidden Necropolis");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void cannotDiscoverWhileAnotherAbilityIsOnStack() {
        addReadyNecropolis();
        harness.addToBattlefield(player1, new HiddenNecropolis());
        addDiscoverMana();
        addDiscoverMana();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hidden Necropolis");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotDiscoverWithoutBlackMana() {
        Permanent necropolis = addReadyNecropolis();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(necropolis.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hidden Necropolis");
    }

    private void addDiscoverMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
