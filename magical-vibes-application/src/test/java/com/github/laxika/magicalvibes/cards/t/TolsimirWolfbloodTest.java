package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
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

@CardUsed({TolsimirWolfblood.class, TrophyHunter.class, CourierHawk.class, GlassGolem.class})
class TolsimirWolfbloodTest extends BaseCardTest {

    @Test
    @DisplayName("Other green and white creatures you control get +1/+1")
    void boostsOtherGreenAndWhiteCreatures() {
        addCreatureReady(player1, new TolsimirWolfblood());
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        Permanent hawk = addCreatureReady(player1, new CourierHawk());

        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost excludes Tolsimir, opponents, and creatures of other colors")
    void excludesSelfOpponentsAndOtherColors() {
        Permanent tolsimir = addCreatureReady(player1, new TolsimirWolfblood());
        Permanent ownGlassGolem = addCreatureReady(player1, new GlassGolem());
        Permanent opponentHunter = addCreatureReady(player2, new TrophyHunter());

        assertThat(gqs.getEffectivePower(gd, tolsimir)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tolsimir)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownGlassGolem)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownGlassGolem)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHunter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHunter)).isEqualTo(3);
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
