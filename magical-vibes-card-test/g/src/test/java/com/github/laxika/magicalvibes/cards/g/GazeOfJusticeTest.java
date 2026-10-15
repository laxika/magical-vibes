package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({GazeOfJustice.class, BenalishCavalry.class, AshcoatBear.class, Plains.class})
class GazeOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature after tapping three untapped white creatures")
    void exilesTargetCreatureAndPaysTapCost() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent third = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

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
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent third = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
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
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent nonwhite = addCreatureReady(player1, new AshcoatBear());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

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

    @Test
    @DisplayName("Cannot pay the additional cost with an already tapped white creature")
    void rejectsTappedWhiteCreatureForTapCost() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent tapped = addCreatureReady(player1, new BenalishCavalry());
        tapped.tap();
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), tapped.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(tapped.isTapped()).isTrue();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Cannot pay the additional cost with a white creature controlled by an opponent")
    void rejectsOpponentControlledWhiteCreatureForTapCost() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent opponentCreature = addCreatureReady(player2, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Cannot pay the additional cost with a noncreature permanent")
    void rejectsNoncreatureForTapCost() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent land = addCreatureReady(player1, new Plains());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent third = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new Plains());

        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(gd.findExiledCard(target.getCard().getId())).isNull();
    }
    @Test
    @DisplayName("Summoning-sick creatures can pay the tap cost, including the target")
    void tapsSummoningSickCreaturesAndExilesOneOfThem() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        GazeOfJustice card = new GazeOfJustice();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryTappingPermanents(player1, 0, first.getId(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.findExiledCard(first.getCard().getId())).isNull();

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("The same creature cannot pay the tap cost more than once")
    void rejectsDuplicateTapCostSelection() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fewer than three creatures cannot pay the tap cost")
    void rejectsTooFewTapCostSelections() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new GazeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot omit the additional tap cost")
    void rejectsFlashbackWithoutTapPayment() {
        Permanent first = addCreatureReady(player1, new BenalishCavalry());
        Permanent second = addCreatureReady(player1, new BenalishCavalry());
        Permanent third = addCreatureReady(player1, new BenalishCavalry());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        GazeOfJustice card = new GazeOfJustice();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }
}
