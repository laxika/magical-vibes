package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StitcherGeralf.class, AzureDrake.class, Forest.class, GrizzlyBears.class, RestInPeace.class})
class StitcherGeralfTest extends BaseCardTest {

    @Test
    void millsEachPlayerExilesTwoCreaturesAndCreatesTokenWithTheirTotalPower() {
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        CardFixture cards = setLibrariesWithTwoCreatures();

        activate(stitcher);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cards.bears().getId(), cards.drake().getId());
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(cards.bears().getId(), cards.drake().getId()));

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(cards.bears(), cards.drake());
        assertThat(stitcher.isTapped()).isTrue();
    }

    @Test
    void mayExileFewerThanTwoCreatures() {
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        activate(stitcher);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Zombie"));
    }

    @Test
    void creatureCardsReplacedOutOfTheGraveyardCannotBeChosen() {
        Permanent stitcher = addCreatureReady(player1, new StitcherGeralf());
        harness.addToBattlefield(player1, new RestInPeace());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        activate(stitcher);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    private CardFixture setLibrariesWithTwoCreatures() {
        Card bears = new GrizzlyBears();
        Card drake = new AzureDrake();
        harness.setLibrary(player1, List.of(bears, drake, new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        return new CardFixture(bears, drake);
    }

    private void activate(Permanent stitcher) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private record CardFixture(Card bears, Card drake) {
    }
}
