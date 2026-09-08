package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconWingedWonder.class, GrizzlyBears.class})
class FalconWingedWonderTest extends BaseCardTest {

    @Test
    void entersWithLegendaryRedwingToken() {
        castFalcon();

        Permanent redwing = findPermanent(player1, "Redwing");
        assertThat(redwing.getCard().isToken()).isTrue();
        assertThat(redwing.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(redwing.getCard().getPower()).isEqualTo(1);
        assertThat(redwing.getCard().getToughness()).isEqualTo(1);
        assertThat(redwing.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD, CardSubtype.SCOUT);
        assertThat(redwing.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(redwing.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
    }

    @Test
    void redwingSurveilsWhenItAttacks() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        castFalcon();

        Permanent redwing = findPermanent(player1, "Redwing");
        redwing.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(redwing)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    private void castFalcon() {
        harness.setHand(player1, List.of(new FalconWingedWonder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
