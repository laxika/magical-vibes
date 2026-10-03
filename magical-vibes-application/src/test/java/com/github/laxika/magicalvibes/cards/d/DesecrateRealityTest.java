package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesecrateReality.class, GrizzlyBears.class, LlanowarElves.class,
        GiantGrowth.class, Forest.class, SolRing.class})
class DesecrateRealityTest extends BaseCardTest {

    @Test
    void exilesOneEvenManaValuePermanentPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(first.getId()), ManaColor.GREEN, 7);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(second.getId());
    }

    @Test
    void rejectsOddManaValueAndControllerPermanents() {
        Permanent odd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(odd.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(own.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adamantReturnsAnOddPermanentFromGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        cast(List.of(target.getId()), ManaColor.COLORLESS, 7);

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Llanowar Elves");
    }

    @Test
    void adamantDoesNotReturnPermanentWithoutThreeColorlessMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        cast(List.of(target.getId()), ManaColor.GREEN, 7);

        assertThat(findPermanents(player1, "Llanowar Elves")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Llanowar Elves");
    }

    @Test
    void canChooseNoExileTargetsEvenWhenOpponentHasALegalPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        cast(List.of(), ManaColor.COLORLESS, 7);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void rejectsTwoTargetsControlledByTheSameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesALandWithZeroManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        cast(List.of(target.getId()), ManaColor.GREEN, 7);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void adamantResolvesWithoutExileTargetsWhenNoLegalPermanentExists() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new SolRing()));

        cast(List.of(), ManaColor.COLORLESS, 7);

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertNotInGraveyard(player1, "Sol Ring");
    }

    @Test
    void adamantReturnsOnlyOddPermanentCardsFromControllersGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(),
                new GiantGrowth(), new SolRing()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        cast(List.of(target.getId()), ManaColor.COLORLESS, 7);

        harness.assertOnBattlefield(player1, "Sol Ring");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Grizzly Bears", "Forest", "Giant Growth")
                .doesNotContain("Sol Ring");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void adamantRequiresChoosingExactlyOneWhenMultipleOddPermanentsExist() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new SolRing()));

        cast(List.of(target.getId()), ManaColor.COLORLESS, 7);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void adamantWorksWithExactlyThreeColorlessManaSpent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void adamantDoesNotWorkWithOnlyTwoColorlessManaSpent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void adamantDoesNotResolveWhenTheOnlyExileTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castInstant(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Desecrate Reality");
    }

    private void cast(List<java.util.UUID> targetIds, ManaColor color, int amount) {
        harness.setHand(player1, List.of(new DesecrateReality()));
        harness.addMana(player1, color, amount);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
