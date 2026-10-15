package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Prohibit;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecruitTheWorthy.class, Prohibit.class})
class RecruitTheWorthyTest extends BaseCardTest {

    @Test
    @DisplayName("Recruit the Worthy creates a 1/1 white Soldier token")
    void createsSoldierToken() {
        RecruitTheWorthy recruit = new RecruitTheWorthy();
        harness.setHand(player1, List.of(recruit));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getPower()).isEqualTo(1);
        assertThat(soldier.getCard().getToughness()).isEqualTo(1);
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(recruit);
    }

    @Test
    @DisplayName("Paying buyback returns Recruit the Worthy to hand as it resolves")
    void buybackReturnsToHand() {
        RecruitTheWorthy recruit = new RecruitTheWorthy();
        harness.setHand(player1, List.of(recruit));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(recruit);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Countering Recruit the Worthy prevents both the token and the buyback return")
    void counteredWithBuybackGoesToGraveyard() {
        RecruitTheWorthy recruit = new RecruitTheWorthy();
        Prohibit prohibit = new Prohibit();
        harness.setHand(player1, List.of(recruit));
        harness.setHand(player2, List.of(prohibit));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.castAndResolveInstant(player2, 0, recruit.getId());

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(recruit);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(prohibit);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Buyback must be paid again on each casting")
    void recastingWithoutBuybackGoesToGraveyard() {
        RecruitTheWorthy recruit = new RecruitTheWorthy();
        harness.setHand(player1, List.of(recruit));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithBuyback(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(recruit);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(recruit);
    }

    @Test
    @DisplayName("Buyback is optional even when enough mana is available")
    void mayDeclineBuybackWithSufficientMana() {
        RecruitTheWorthy recruit = new RecruitTheWorthy();
        harness.setHand(player1, List.of(recruit));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(recruit);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }
}
