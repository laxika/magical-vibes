package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyrMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Myr creatures get +1/+1 regardless of controller")
    void buffsAllMyrCreatures() {
        harness.addToBattlefield(player1, new MyrMatrix());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent ownMyr = findPermanent(player1, "Gold Myr");
        Permanent opponentMyr = findPermanent(player2, "Gold Myr");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownMyr)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
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
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MYR);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
