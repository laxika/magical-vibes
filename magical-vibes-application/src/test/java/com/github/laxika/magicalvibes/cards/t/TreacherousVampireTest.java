package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreacherousVampire.class, GiantGrowth.class, Shock.class})
class TreacherousVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 with seven cards in its controller's graveyard")
    void getsThresholdBoost() {
        fillGraveyard(player1, 7);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(6);
    }

    @Test
    @DisplayName("Exiles a graveyard card instead of sacrificing when it attacks")
    void exilesCardInsteadOfSacrificingWhenAttacking() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);
        Card cardToKeep = new Shock();
        Card cardToExile = new GiantGrowth();
        harness.setGraveyard(player1, List.of(cardToKeep, cardToExile));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vampire);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cardToKeep);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(cardToExile);
    }

    @Test
    @DisplayName("Sacrifices itself when its attack trigger is declined")
    void sacrificesWhenAttackChoiceIsDeclined() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);
        Card cardInGraveyard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(cardInGraveyard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardInGraveyard, vampire.getCard());
    }

    @Test
    @DisplayName("Threshold death ability makes its controller lose 6 life")
    void thresholdDeathAbilityLosesLife() {
        fillGraveyard(player1, 7);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Threshold death ability is absent below seven graveyard cards")
    void thresholdDeathAbilityIsAbsentBelowThreshold() {
        fillGraveyard(player1, 6);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void fillGraveyard(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        harness.setGraveyard(player, cards);
    }
}
