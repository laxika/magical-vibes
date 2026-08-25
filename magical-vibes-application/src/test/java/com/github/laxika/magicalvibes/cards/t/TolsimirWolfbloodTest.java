package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolsimirWolfblood.class, GrizzlyBears.class, SavannahLions.class, Memnite.class})
class TolsimirWolfbloodTest extends BaseCardTest {

    @Test
    @DisplayName("Other green and white creatures you control get +1/+1")
    void boostsOtherGreenAndWhiteCreatures() {
        addCreatureReady(player1, new TolsimirWolfblood());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent lions = addCreatureReady(player1, new SavannahLions());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lions)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lions)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost excludes Tolsimir, opponents, and creatures of other colors")
    void excludesSelfOpponentsAndOtherColors() {
        Permanent tolsimir = addCreatureReady(player1, new TolsimirWolfblood());
        Permanent ownMemnite = addCreatureReady(player1, new Memnite());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, tolsimir)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tolsimir)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownMemnite)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownMemnite)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping Tolsimir creates a legendary Voja token")
    void createsLegendaryVojaToken() {
        Permanent tolsimir = addCreatureReady(player1, new TolsimirWolfblood());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent voja = findPermanent(player1, "Voja");
        assertThat(tolsimir.isTapped()).isTrue();
        assertThat(voja.getCard().isToken()).isTrue();
        assertThat(voja.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(voja.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(voja.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(voja.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(voja.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(voja.getCard().getPower()).isEqualTo(2);
        assertThat(voja.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, voja)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, voja)).isEqualTo(4);
    }
}
