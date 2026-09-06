package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirbendingLesson.class, Forest.class, GrizzlyBears.class})
class AirbendingLessonTest extends BaseCardTest {

    @Test
    @DisplayName("Airbends a nonland permanent and draws a card")
    void airbendsPermanentAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.setLibrary(player1, List.of(new Forest()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId()))
                .isEqualTo(player2.getId());
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The target's owner can cast an airbent permanent for {2}")
    void targetOwnerCanCastAirbentPermanentForTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, target.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
