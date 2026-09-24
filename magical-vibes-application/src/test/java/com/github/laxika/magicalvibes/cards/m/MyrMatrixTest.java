package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcboundWorker;
import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
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

@CardUsed({MyrMatrix.class, MyrMoonvessel.class, ArcboundWorker.class, EchoingRuin.class})
class MyrMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Myr creatures get +1/+1 regardless of controller")
    void buffsAllMyrCreatures() {
        harness.addToBattlefield(player1, new MyrMatrix());
        Permanent ownMyr = harness.enterBattlefieldAndReturn(player1, new MyrMoonvessel());
        Permanent opponentMyr = harness.enterBattlefieldAndReturn(player2, new MyrMoonvessel());
        Permanent nonMyr = harness.enterBattlefieldAndReturn(player1, new ArcboundWorker());

        assertThat(gqs.getEffectivePower(gd, ownMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownMyr)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonMyr)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability creates a 1/1 colorless Myr artifact creature token")
    void createsMyrToken() {
        harness.addToBattlefield(player1, new MyrMatrix());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Myr");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MYR);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("Myr Matrix survives a destroy effect")
    void survivesDestroyEffect() {
        Permanent matrix = harness.addToBattlefieldAndReturn(player2, new MyrMatrix());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, matrix.getId());

        harness.assertOnBattlefield(player2, "Myr Matrix");
        harness.assertNotInGraveyard(player2, "Myr Matrix");
    }
}
