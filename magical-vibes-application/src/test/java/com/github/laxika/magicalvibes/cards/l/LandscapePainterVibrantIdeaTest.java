package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LandscapePainterVibrantIdea.class})
class LandscapePainterVibrantIdeaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Landscape Painter and exiles a Vibrant Idea copy")
    void entersPrepared() {
        Permanent painter = castLandscapePainter();

        assertThat(painter.isPrepared()).isTrue();
        UUID copyId = painter.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting Vibrant Idea unprepares Landscape Painter and draws two cards")
    void castingPrepareCopyDrawsTwoCards() {
        Permanent painter = castLandscapePainter();
        UUID copyId = painter.getPreparedSpellCardId();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(painter.isPrepared()).isFalse();
        assertThat(painter.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Entering prepared creates no triggered ability on the stack")
    void enteringPreparedDoesNotTrigger() {
        harness.castFromHand(player1, new LandscapePainterVibrantIdea(), "{1}{U}");
        harness.passBothPriorities();

        Permanent painter = findPermanent(player1, "Landscape Painter");
        assertThat(painter.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(painter.getPreparedSpellCardId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting the prepare spell unprepares its source before resolution")
    void unpreparesAsSpellIsCast() {
        Permanent painter = castLandscapePainter();
        UUID copyId = painter.getPreparedSpellCardId();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromExile(player1, copyId);

        assertThat(painter.isPrepared()).isFalse();
        assertThat(painter.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("An unpaid prepare spell leaves its source prepared and its copy in exile")
    void failedManaPaymentPreservesPreparation() {
        Permanent painter = castLandscapePainter();
        UUID copyId = painter.getPreparedSpellCardId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(painter.isPrepared()).isTrue();
        assertThat(painter.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vibrant Idea cannot be cast outside a main phase")
    void prepareSpellRequiresSorceryTiming() {
        Permanent painter = castLandscapePainter();
        UUID copyId = painter.getPreparedSpellCardId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");

        assertThat(painter.isPrepared()).isTrue();
        assertThat(painter.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castLandscapePainter() {
        harness.castFromHand(player1, new LandscapePainterVibrantIdea(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Landscape Painter");
    }
}
