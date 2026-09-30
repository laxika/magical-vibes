package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogsDreadsire.class, Forest.class, GrizzlyBears.class, Shock.class})
class UlamogsDreadsireTest extends BaseCardTest {

    @Test
    void createsTenTenEldraziToken() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        dreadsire.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(10);
        assertThat(token.getEffectiveToughness()).isEqualTo(10);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELDRAZI);
    }

    @Test
    void wardCannotBePaidWithZeroManaValuePermanent() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.addToBattlefield(player2, new Forest());
        prepareOpponentShock(dreadsire);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void wardCanBePaidBySacrificingPermanentWithManaValueAtLeastOne() {
        Permanent dreadsire = harness.addToBattlefieldAndReturn(player1, new UlamogsDreadsire());
        harness.addToBattlefield(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareOpponentShock(dreadsire);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId())
                .doesNotContain(gd.playerBattlefields.get(player2.getId()).stream()
                        .filter(permanent -> permanent.getCard() instanceof Forest)
                        .findFirst()
                        .orElseThrow()
                        .getId());
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void prepareOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
