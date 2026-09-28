package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanceWithCalamity.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class})
class DanceWithCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Can stop exiling immediately and leave the library unchanged")
    void canStopWithoutExiling() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiles repeatedly, then offers exiled spells for free casting within the limit")
    void exilesAndCastsWithinManaValueLimit() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears, new Forest(), new Forest(), new Forest()));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not offer free casts when the exiled total exceeds thirteen")
    void exceedingManaValueLimitPreventsCasting() {
        AvatarOfMight first = new AvatarOfMight();
        AvatarOfMight second = new AvatarOfMight();
        harness.setLibrary(player1, List.of(first, second));

        castDanceWithCalamity();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(first.getId())
                || entry.getCard().getId().equals(second.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castDanceWithCalamity() {
        harness.setHand(player1, List.of(new DanceWithCalamity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
