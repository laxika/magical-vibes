package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForbiddenFriendship.class, EsixFractalBloom.class, AlmightyBrushwagg.class})
class ForbiddenFriendshipTest extends BaseCardTest {

    @Test
    void createsHastyDinosaurAndHumanSoldierTokens() {
        harness.castFromHand(player1, new ForbiddenFriendship(), "{1}{R}");
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

    @Test
    void esixReplacesBothTokensAsOneCreationEvent() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());

        harness.castFromHand(player1, new ForbiddenFriendship(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, brushwagg.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(permanent ->
                        assertThat(permanent.getCard().getName()).isEqualTo("Almighty Brushwagg"));
        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }
}
