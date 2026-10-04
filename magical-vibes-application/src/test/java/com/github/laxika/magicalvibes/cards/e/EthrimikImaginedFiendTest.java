package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EthrimikImaginedFiend.class, GrizzlyBears.class, Forest.class})
class EthrimikImaginedFiendTest extends BaseCardTest {

    @Test
    void entersAndManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new EthrimikImaginedFiend()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        addEthrimikMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void boostsOtherCreaturesYouControlButNotItselfOrOpposingCreatures() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ethrimik = addCreatureReady(player1, new EthrimikImaginedFiend());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ethrimik)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ethrimik)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    @Test
    void canAttackWithoutAnotherCreatureYouControl() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new EthrimikImaginedFiend());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void cannotAttackWithAnotherCreatureYouControl() {
        addCreatureReady(player1, new EthrimikImaginedFiend());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithoutAnotherCreatureYouControl() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new EthrimikImaginedFiend());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    void cannotBlockWithAnotherCreatureYouControl() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new EthrimikImaginedFiend());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestedLandIsBoostedAndPreventsEthrimikFromAttacking() {
        Card land = new Forest();
        harness.setHand(player1, List.of(new EthrimikImaginedFiend()));
        harness.setLibrary(player1, List.of(land));
        addEthrimikMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        findPermanent(player1, "Ethrimik, Imagined Fiend").setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addEthrimikMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
