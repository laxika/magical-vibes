package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LaeliaTheBladeReforged;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ContestOfClaws.class, AirElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class})
class ContestOfClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the first target's power without discovering when there is no excess")
    void doesNotDiscoverWithoutExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        cast(source, target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears);
    }

    @Test
    @DisplayName("Discovers the excess damage amount")
    void discoversForExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(forest, discovered));

        cast(source, target);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a noncreature second target")
    void rejectsNoncreatureSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Forest land = new Forest();
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new ContestOfClaws()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(),
                harness.getPermanentId(player2, "Forest"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void requiresASecondCreatureTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ContestOfClaws()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTheSameCreatureForBothTargets() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ContestOfClaws()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAnOpponentsCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ContestOfClaws()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDealDamageToAnotherCreatureYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        cast(source, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotDiscoverForExactlyLethalDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LlanowarElves top = new LlanowarElves();
        harness.setLibrary(player1, List.of(top));

        cast(source, target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void accountsForDamageAlreadyMarkedWhenCalculatingExcess() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setMarkedDamage(2);
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));

        cast(source, target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void skipsExpensiveCardsAndCastsTheDiscoveredCardWithoutMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        HillGiant tooExpensive = new HillGiant();
        LlanowarElves discovered = new LlanowarElves();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, tooExpensive, discovered, remaining));

        cast(source, target);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(remaining);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(forest, tooExpensive);
    }

    @Test
    @CardUsed({LaeliaTheBladeReforged.class})
    void discoveringTriggersLaeliaForEachCardExiledFromTheLibrary() {
        Permanent laelia = harness.addToBattlefieldAndReturn(player1, new LaeliaTheBladeReforged());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves()));

        cast(source, target);
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(laelia.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void cast(Permanent source, Permanent target) {
        harness.setHand(player1, List.of(new ContestOfClaws()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), target.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
