package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KuraTheBoundlessSky;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ShigekiJukaiVisionary.class, Forest.class, GrizzlyBears.class, KuraTheBoundlessSky.class})
class ShigekiJukaiVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability may put a revealed land onto the battlefield tapped")
    void activatedAbilityPutsLandOntoBattlefieldTappedAndRestIntoGraveyard() {
        Permanent shigeki = harness.addToBattlefieldAndReturn(player1, new ShigekiJukaiVisionary());
        shigeki.setSummoningSick(false);
        Forest forest = new Forest();
        List<Card> nonlands = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(forest, nonlands.get(0), nonlands.get(1), nonlands.get(2)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(nonlands);
        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
    }

    @Test
    @DisplayName("Channel returns exactly X target nonlegendary cards and discards the source")
    void channelReturnsXNonlegendaryCards() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card legendary = new KuraTheBoundlessSky();
        harness.setGraveyard(player1, List.of(first, second, legendary));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(legendary)
                .anyMatch(card -> card.getName().equals("Shigeki, Jukai Visionary"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Channel cannot choose fewer targets than X when enough legal cards exist")
    void channelRequiresExactlyXTargets() {
        harness.setHand(player1, List.of(new ShigekiJukaiVisionary()));
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shigeki, Jukai Visionary");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }
}
