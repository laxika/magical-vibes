package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoldeviSage.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class SoldeviSageTest extends BaseCardTest {

    private void seedLibrary() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));
    }

    @Test
    @DisplayName("Activating with exactly two lands auto-sacrifices both and taps the Sage")
    void activatesWithTwoLands() {
        Permanent sage = addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);

        assertThat(sage.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving draws three cards then discards one (net +2 to hand)")
    void resolvingDrawsThreeThenDiscardsOne() {
        addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only cards drawn by the ability can be discarded")
    void onlyDrawnCardsCanBeDiscarded() {
        addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Prompts which lands to sacrifice when more than two are available")
    void promptsForLandChoiceWithMoreThanTwoLands() {
        addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        seedLibrary();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only lands are valid sacrifice choices, not other permanents")
    void onlyLandsAreSacrificed() {
        addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());
        seedLibrary();

        UUID forestId = findPermanent(player1, "Forest").getId();
        UUID islandId = findPermanent(player1, "Island").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forestId);
        harness.handlePermanentChosen(player1, islandId);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Soldevi Sage");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Cannot activate with fewer than two lands")
    void cannotActivateWithoutTwoLands() {
        addCreatureReady(player1, new SoldeviSage());
        harness.addToBattlefield(player1, new Forest());
        seedLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        Permanent sage = new Permanent(new SoldeviSage());
        gd.playerBattlefields.get(player1.getId()).add(sage);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        seedLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
