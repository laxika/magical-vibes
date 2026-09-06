package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantRescuer.class, Censor.class, GrizzlyBears.class})
class ValiantRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("The first cycling each turn creates a 1/1 Human Soldier")
    void firstCycleEachTurnCreatesHumanSoldier() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("A second cycling in the same turn does not create another token")
    void secondCycleSameTurnDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new ValiantRescuer());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Censor");
    }
}
