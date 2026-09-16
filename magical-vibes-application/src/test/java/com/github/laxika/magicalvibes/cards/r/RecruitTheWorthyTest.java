package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RecruitTheWorthy.class})
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
}
