package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerrowSkyswimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white and blue Merfolk token")
    void createsMerfolkToken() {
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Merfolk");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Convoke taps creatures to help cast Merrow Skyswimmer")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MerrowSkyswimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId(), thirdCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(thirdCreature.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MerrowSkyswimmer);
        assertThat(findPermanents(player1, "Merfolk")).hasSize(1);
    }
}
