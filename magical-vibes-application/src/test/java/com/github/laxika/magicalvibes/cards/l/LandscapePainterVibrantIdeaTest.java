package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent castLandscapePainter() {
        harness.setHand(player1, List.of(new LandscapePainterVibrantIdea()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Landscape Painter");
    }
}
