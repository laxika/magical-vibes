package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenScorcher;
import com.github.laxika.magicalvibes.cards.e.EarsplittingRats;
import com.github.laxika.magicalvibes.cards.k.KrosanWayfarer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenScorcher.class, EarsplittingRats.class, KrosanWayfarer.class, TreacherousVampire.class})
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
    @DisplayName("Counts only its controller's graveyard for threshold")
    void thresholdCountsItsControllersGraveyard() {
        fillGraveyard(player2, 7);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exiles a graveyard card instead of sacrificing when it attacks")
    void exilesCardInsteadOfSacrificingWhenAttacking() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);
        Card cardToKeep = new KrosanWayfarer();
        Card cardToExile = new DwarvenScorcher();
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
    @DisplayName("Exiles a graveyard card instead of sacrificing when it blocks")
    void exilesCardInsteadOfSacrificingWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new KrosanWayfarer());
        attacker.setAttacking(true);
        Permanent vampire = addCreatureReady(player2, new TreacherousVampire());
        Card cardToKeep = new KrosanWayfarer();
        Card cardToExile = new DwarvenScorcher();
        harness.setGraveyard(player2, List.of(cardToKeep, cardToExile));

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vampire);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(cardToKeep);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(cardToExile);
    }

    @Test
    @DisplayName("Sacrifices itself when its attack trigger is declined")
    void sacrificesWhenAttackChoiceIsDeclined() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);
        Card cardInGraveyard = new KrosanWayfarer();
        harness.setGraveyard(player1, List.of(cardInGraveyard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardInGraveyard, vampire.getCard());
    }

    @Test
    @DisplayName("Sacrifices itself without a choice when its attack trigger has no graveyard card to exile")
    void sacrificesWhenAttackHasNoGraveyardCard() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vampire.getCard());
    }

    @Test
    @DisplayName("Exiling the seventh graveyard card removes the threshold bonus")
    void exilingSeventhGraveyardCardRemovesThresholdBonus() {
        fillGraveyard(player1, 7);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new TreacherousVampire());
        vampire.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 6);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
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
            cards.add(new KrosanWayfarer());
        }
        harness.setGraveyard(player, cards);
    }

    @Test
    @DisplayName("Sacrifices itself when it blocks and its choice is declined")
    void sacrificesWhenBlockChoiceIsDeclined() {
        Permanent attacker = addCreatureReady(player1, new EarsplittingRats());
        attacker.setAttacking(true);
        Permanent vampire = addCreatureReady(player2, new TreacherousVampire());
        Card cardInGraveyard = new EarsplittingRats();
        harness.setGraveyard(player2, List.of(cardInGraveyard));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(vampire),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(vampire);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(cardInGraveyard, vampire.getCard());
    }

    @Test
    @DisplayName("Sacrifices itself when it attacks with no card in its graveyard")
    void sacrificesWhenAttackingWithEmptyGraveyard() {
        Permanent vampire = addCreatureReady(player1, new TreacherousVampire());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vampire.getCard());
    }

    @Test
    @DisplayName("Loses threshold abilities after exiling its seventh graveyard card")
    void losesThresholdAbilitiesAfterExilingSeventhGraveyardCard() {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graveyard.add(new EarsplittingRats());
        }
        harness.setGraveyard(player1, graveyard);
        Permanent vampire = addCreatureReady(player1, new TreacherousVampire());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(6);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);

        vampire.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        harness.assertLife(player1, 20);
    }
}
