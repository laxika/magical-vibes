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

        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Citizen");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A lone Darling does not boost itself or opposing Citizens")
    void excludesSelfAndOpposingCitizens() {
        Permanent darling = harness.addToBattlefieldAndReturn(player1, new DarlingOfTheMasses());
        Permanent opposingDarling = harness.addToBattlefieldAndReturn(player2, new DarlingOfTheMasses());

        assertThat(gqs.getEffectivePower(gd, darling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, darling)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingDarling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingDarling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each attacking Darling creates one Citizen and both boosts apply")
    void eachAttackingDarlingCreatesOneToken() {
        addCreatureReady(player1, new DarlingOfTheMasses());
        addCreatureReady(player1, new DarlingOfTheMasses());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isAttacking()).isFalse();
        });
        assertThat(findPermanents(player2, "Citizen")).isEmpty();
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
