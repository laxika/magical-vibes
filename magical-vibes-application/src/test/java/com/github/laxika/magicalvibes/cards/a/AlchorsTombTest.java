package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchorsTomb.class, GrizzlyBears.class})
class AlchorsTombTest extends BaseCardTest {

    @Test
    void targetBecomesOneChosenColorIndefinitely() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);

        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    void canTargetOnlyAPermanentYouControl() {
        harness.addToBattlefield(player1, new AlchorsTomb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
