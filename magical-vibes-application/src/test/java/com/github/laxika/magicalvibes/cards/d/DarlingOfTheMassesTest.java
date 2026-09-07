package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarlingOfTheMasses.class, GrizzlyBears.class})
class DarlingOfTheMassesTest extends BaseCardTest {

    @Test
    @DisplayName("Other Citizens you control get +1/+0")
    void buffsOtherCitizensYouControl() {
        Permanent firstDarling = harness.addToBattlefieldAndReturn(player1, new DarlingOfTheMasses());
        Permanent secondDarling = harness.addToBattlefieldAndReturn(player1, new DarlingOfTheMasses());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, firstDarling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstDarling)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondDarling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondDarling)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creates a green and white Citizen token")
    void attackingCreatesCitizenToken() {
        addCreatureReady(player1, new DarlingOfTheMasses());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Citizen").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token when it does not attack")
    void noTokenWhenNotAttacking() {
        addCreatureReady(player1, new DarlingOfTheMasses());

        declareAttackers(List.of());

        assertThat(findPermanents(player1, "Citizen").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
