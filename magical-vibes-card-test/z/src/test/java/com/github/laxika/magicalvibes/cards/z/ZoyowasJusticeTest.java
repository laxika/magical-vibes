package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZoyowasJustice.class, Millstone.class, HillGiant.class, LlanowarElves.class,
        Plains.class, Ornithopter.class, GloriousAnthem.class})
class ZoyowasJusticeTest extends BaseCardTest {

    @Test
    void shufflesTargetArtifactAndItsOwnerDiscoversUsingManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player2, List.of(new Plains(), new HillGiant(), discovered));
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).hasSize(1);
        Card found = search.params().cards().getFirst();
        assertThat(found).isIn(discovered, target.getCard());
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.playerHands.get(player2.getId())).contains(found);
    }

    @Test
    void cannotTargetAZeroManaValueOrNonArtifactCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature with mana value 1 or greater");
    }

    @Test
    void canRediscoverAndCastTheShuffledCreatureAtExactlyItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(target.getCard());

        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void cannotTargetAZeroManaValueArtifactCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature with mana value 1 or greater");
    }

    @Test
    void cannotTargetANonArtifactNonCreatureWithPositiveManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature with mana value 1 or greater");
    }

    @Test
    void ownerDiscoversEvenWhenAnotherPlayerControlsTheTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Millstone());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).containsExactly(target.getCard());
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player1, "Millstone");
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotDiscoverWhenTheTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new ZoyowasJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player2, "Millstone");
    }
}
