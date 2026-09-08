package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesperateSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Desperate Sentry +3/+0")
    void deliriumBoostsDesperateSentry() {
        Permanent sentry = addSentry(player1);

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
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Desperate Sentry"));
        harness.passBothPriorities();
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

    private Permanent addSentry(Player player) {
        Permanent sentry = new Permanent(new DesperateSentry());
        sentry.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sentry);
        return sentry;
    }
}
