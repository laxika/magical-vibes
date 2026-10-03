package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateSentry.class, GrizzlyBears.class, Plains.class, Shock.class, Millstone.class,
        FieldCreeper.class})
class DesperateSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Desperate Sentry +3/+0")
    void deliriumBoostsDesperateSentry() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new DesperateSentry());

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Desperate Sentry dies, it creates a 3/2 colorless Eldrazi Horror token")
    void deathCreatesEldraziHorrorToken() {
        harness.addToBattlefield(player1, new DesperateSentry());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Desperate Sentry"));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Eldrazi Horror"))
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
    }

    @Test
    @DisplayName("An artifact creature contributes both card types to delirium")
    void multipleTypesOnOneCardEnableDelirium() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new DesperateSentry());
        harness.setGraveyard(player1, List.of(new FieldCreeper(), new Plains(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Four cards with only three distinct types do not enable delirium")
    void repeatedTypesDoNotEnableDelirium() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new DesperateSentry());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Plains(), new Shock()
        ));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("The delirium bonus disappears immediately when fewer than four types remain")
    void deliriumBonusIsRemovedWhenTypesDecrease() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new DesperateSentry());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Plains(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Delirium counts only the controller's graveyard and boosts only the Sentry")
    void deliriumIsControllerAndSelfScoped() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new DesperateSentry());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Plains(), new Shock(), new Millstone()
        ));

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }
}
