package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuratorBeastie.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class CuratorBeastieTest extends BaseCardTest {

    @Test
    void colorlessCreaturesEnterWithTwoAdditionalCounters() {
        harness.addToBattlefield(player1, new CuratorBeastie());

        Permanent colorlessCreature = harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        Permanent coloredCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentsColorlessCreature = harness.enterBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(colorlessCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(coloredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentsColorlessCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new CuratorBeastie()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void attackingManifestsDread() {
        Permanent curator = addCreatureReady(player1, new CuratorBeastie());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(curator.isTapped()).isTrue();
    }

    @Test
    void multipleCuratorsGiveManifestedColoredCreatureFourCounters() {
        harness.addToBattlefield(player1, new CuratorBeastie());
        addCreatureReady(player1, new CuratorBeastie());
        Card manifestedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));

        declareAttackers(List.of(1));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void mayManifestSecondCardEvenWhenItIsALand() {
        addCreatureReady(player1, new CuratorBeastie());
        Card graveyardCard = new GrizzlyBears();
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(graveyardCard, manifestedCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestDreadWithOneCardManifestsItWithoutPuttingAnythingIntoGraveyard() {
        addCreatureReady(player1, new CuratorBeastie());
        Card manifestedCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void faceDownCuratorDoesNotGrantEntryCounters() {
        Permanent faceUpCurator = addCreatureReady(player1, new CuratorBeastie());
        Card manifestedCard = new CuratorBeastie();
        harness.setLibrary(player1, List.of(manifestedCard, new Forest()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(faceUpCurator);

        Permanent enteringCreature = harness.enterBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
