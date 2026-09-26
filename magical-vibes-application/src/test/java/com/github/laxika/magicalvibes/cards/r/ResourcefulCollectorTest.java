package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResourcefulCollector.class, GrizzlyBears.class, Shock.class})
class ResourcefulCollectorTest extends BaseCardTest {

    @Test
    void makesAnEligibleGraveyardPermanentFoodAndPlayable() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new Shock()));

        advanceToEndStep();

        Card foodCard = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(foodCard.getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(foodCard.getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(gd.graveyardPlayPermissions).containsEntry(foodCard.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, food, CardSubtype.FOOD)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
