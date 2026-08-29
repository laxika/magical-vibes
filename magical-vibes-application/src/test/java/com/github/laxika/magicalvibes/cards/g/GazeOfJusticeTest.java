package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.t.GazeOfJustice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GazeOfJustice.class, SavannahLions.class, GrizzlyBears.class})
class GazeOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature after tapping three untapped white creatures")
    void exilesTargetCreatureAndPaysTapCost() {
        Permanent first = addCreatureReady(player1, new SavannahLions());
        Permanent second = addCreatureReady(player1, new SavannahLions());
        Permanent third = addCreatureReady(player1, new SavannahLions());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Flashback also requires tapping three untapped white creatures")
    void flashbackPaysAdditionalTapCost() {
        Permanent first = addCreatureReady(player1, new SavannahLions());
        Permanent second = addCreatureReady(player1, new SavannahLions());
        Permanent third = addCreatureReady(player1, new SavannahLions());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        GazeOfJustice card = new GazeOfJustice();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashbackWithAdditionalCostTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @DisplayName("Cannot pay the additional cost with a nonwhite creature")
    void rejectsNonwhiteCreatureForTapCost() {
        Permanent first = addCreatureReady(player1, new SavannahLions());
        Permanent second = addCreatureReady(player1, new SavannahLions());
        Permanent nonwhite = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), nonwhite.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(nonwhite.isTapped()).isFalse();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }
}
