package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuarryColossus.class, GrizzlyBears.class, Island.class, Plains.class})
class QuarryColossusTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts target creature beneath the number of cards equal to Plains controlled")
    void putsTargetCreatureBeneathPlainsCount() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Island()));

        castQuarryColossus(bears);

        var library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(4);
        assertThat(library.get(2).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB puts target creature on top when no Plains are controlled")
    void putsTargetCreatureOnTopWithNoPlains() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Island(), new Island()));

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB uses Plains controlled at resolution and ignores opposing Plains and other lands")
    void countsCurrentPlainsWhenTriggerResolves() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island first = new Island();
        Island second = new Island();
        Island third = new Island();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactly(first, second, bears.getCard(), third);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB puts the creature on the bottom when fewer cards than Plains remain")
    void putsCreatureOnBottomOfShortLibrary() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island first = new Island();
        harness.setLibrary(player2, List.of(first));

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, bears.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB puts the creature into an empty library")
    void putsCreatureIntoEmptyLibrary() {
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears.getCard());
    }

    @Test
    @DisplayName("ETB can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(first, bears.getCard(), second);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does nothing if its target leaves before resolution")
    void doesNotMoveTargetThatLeftBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island first = new Island();
        harness.setLibrary(player2, List.of(first));
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Quarry Colossus");
    }

    @Test
    @DisplayName("ETB puts a stolen creature into its owner's library")
    void putsStolenCreatureIntoOwnersLibrary() {
        harness.addToBattlefield(player1, new Plains());
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        Island first = new Island();
        Island second = new Island();
        Island ownCard = new Island();
        harness.setLibrary(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(ownCard));

        castQuarryColossus(bears);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, card, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB still resolves after Quarry Colossus leaves and uses the remaining Plains")
    void triggerResolvesWithoutSourceUsingRemainingPlains() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Island first = new Island();
        harness.setLibrary(player2, List.of(first));
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();

        Permanent colossus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof QuarryColossus)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, colossus);
            harness.getPermanentRemovalService().removePermanentToHand(gd, plains);
        });
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears.getCard(), first);
        harness.assertInHand(player1, "Quarry Colossus");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void castQuarryColossus(Permanent target) {
        harness.setHand(player1, List.of(new QuarryColossus()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
