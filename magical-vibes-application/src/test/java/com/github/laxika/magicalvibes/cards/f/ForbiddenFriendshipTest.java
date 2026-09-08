package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForbiddenFriendship.class})
class ForbiddenFriendshipTest extends BaseCardTest {

    @Test
    void createsHastyDinosaurAndHumanSoldierTokens() {
        harness.setHand(player1, List.of(new ForbiddenFriendship()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> dinosaurs = findPermanents(player1, "Dinosaur");
        assertThat(dinosaurs).hasSize(1);
        assertThat(dinosaurs.getFirst().getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dinosaurs.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(dinosaurs.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(dinosaurs.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, dinosaurs.getFirst(), Keyword.HASTE)).isTrue();

        List<Permanent> humanSoldiers = findPermanents(player1, "Human Soldier");
        assertThat(humanSoldiers).hasSize(1);
        assertThat(humanSoldiers.getFirst().getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(humanSoldiers.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(humanSoldiers.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(humanSoldiers.getFirst().getCard().getSubtypes())
                .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, humanSoldiers.getFirst(), Keyword.HASTE)).isFalse();

        assertThat(findPermanents(player2, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player2, "Human Soldier")).isEmpty();
    }
}
