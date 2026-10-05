package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndomitableCreativity.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        LeylineOfTheVoid.class, CosisTrickster.class})
class IndomitableCreativityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys multiple targets and puts one matching card onto each controller's battlefield")
    void destroysTargetsAndReplacesThemForEachController() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new FountainOfYouth()));

        castIndomitableCreativity(2, List.of(ownTarget.getId(), opponentTarget.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Destroyed permanents controlled by one player grant that player one reveal per permanent")
    void revealsOnceForEachDestroyedPermanent() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new Forest(), new FountainOfYouth(), new Forest(), new GrizzlyBears()));

        castIndomitableCreativity(2, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstTarget.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Fountain of Youth"))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2)
                .allMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("An indestructible target does not count toward the replacement cards")
    void indestructibleTargetDoesNotCount() {
        Card indestructibleCard = new GrizzlyBears();
        indestructibleCard.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, indestructibleCard);
        Permanent destroyable = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castIndomitableCreativity(2, List.of(indestructible.getId(), destroyable.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible)
                .noneMatch(permanent -> permanent.getId().equals(destroyable.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && !permanent.getId().equals(indestructible.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast(1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresExactlyXTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXResolvesWithoutTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        prepareCast(0);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Indomitable Creativity");
    }

    @Test
    void destroyedCreatureExiledInsteadOfDyingStillGrantsReplacement() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        GrizzlyBears destroyedCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, destroyedCard);
        FountainOfYouth replacement = new FountainOfYouth();
        harness.setLibrary(player2, List.of(new Forest(), replacement));

        castIndomitableCreativity(1, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(replacement.getId()));
        assertThat(gd.findExiledCard(destroyedCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void revealedCreatureSeesOpponentsFinalShuffle() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new CosisTrickster()));
        harness.setLibrary(player2, List.of(new FountainOfYouth(), new Forest()));

        castIndomitableCreativity(2, List.of(ownTarget.getId(), opposingTarget.getId()));

        harness.assertOnBattlefield(player1, "Cosi's Trickster");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CosisTrickster.class);
    }

    @Test
    void affectedPlayerShufflesEvenAnEmptyLibrary() {
        harness.addToBattlefield(player1, new CosisTrickster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castIndomitableCreativity(1, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(CosisTrickster.class);
    }

    @Test
    void libraryWithoutMatchingCardsIsKeptAndShuffled() {
        harness.addToBattlefield(player1, new CosisTrickster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        castIndomitableCreativity(1, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void exhaustedLibraryCanProvideFewerCardsThanDestroyedPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        GrizzlyBears replacement = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest, replacement));

        castIndomitableCreativity(2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.getCard().getId().equals(replacement.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
    }

    private void castIndomitableCreativity(int xValue, List<UUID> targetIds) {
        prepareCast(xValue);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }

    private void prepareCast(int xValue) {
        harness.setHand(player1, List.of(new IndomitableCreativity()));
        harness.addMana(player1, ManaColor.RED, xValue + 3);
    }
}
