package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TolsimirWolfblood.class, TrophyHunter.class, CourierHawk.class, GlassGolem.class, Putrefy.class})
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

    @Test
    @DisplayName("Voja is still created if Tolsimir dies before his ability resolves")
    void createsVojaAfterSourceLeavesBattlefield() {
        Permanent tolsimir = addCreatureReady(player1, new TolsimirWolfblood());
        harness.setHand(player2, List.of(new Putrefy()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tolsimir.getId());
        harness.assertInGraveyard(player1, "Tolsimir Wolfblood");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Voja")).isEqualTo(1);
        assertThat(countPermanents(player2, "Voja")).isZero();
        Permanent voja = findPermanent(player1, "Voja");
        assertThat(gqs.getEffectivePower(gd, voja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, voja)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both color boosts end when Tolsimir leaves the battlefield")
    void losesBoostsWhenTolsimirLeavesBattlefield() {
        Permanent tolsimir = addCreatureReady(player1, new TolsimirWolfblood());
        Permanent hunter = addCreatureReady(player1, new TrophyHunter());
        Permanent hawk = addCreatureReady(player1, new CourierHawk());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent voja = findPermanent(player1, "Voja");
        assertThat(gqs.getEffectivePower(gd, voja)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, voja)).isEqualTo(4);

        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, tolsimir.getId());

        harness.assertInGraveyard(player1, "Tolsimir Wolfblood");
        assertThat(gqs.getEffectivePower(gd, hunter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hunter)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, voja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, voja)).isEqualTo(2);
    }
}
