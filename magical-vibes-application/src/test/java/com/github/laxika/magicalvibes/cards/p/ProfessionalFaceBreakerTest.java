package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfessionalFaceBreaker.class, GrizzlyBears.class, Forest.class})
class ProfessionalFaceBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates only one Treasure when multiple creatures deal combat damage to a player")
    void createsOneTreasureForMultipleCombatDamageDealers() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Treasure exiles the top card with permission to play it this turn")
    void sacrificeTreasureExilesTopCardWithPlayPermission() {
        addCreatureReady(player1, new ProfessionalFaceBreaker());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }
}
