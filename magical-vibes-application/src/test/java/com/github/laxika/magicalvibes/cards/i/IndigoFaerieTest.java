package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IndigoFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("{U}: target keeps its own colors and gains blue")
    void targetGainsBlueInAdditionToOtherColors() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanent(player1, "Grizzly Bears");
        // Green Grizzly Bears stays green and additionally becomes blue.
        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
    }

    @Test
    @DisplayName("A colorless permanent simply becomes blue")
    void colorlessTargetBecomesBlue() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID faerieId = harness.getPermanentId(player1, "Indigo Faerie");
        harness.activateAbility(player1, 0, 0, null, faerieId);
        harness.passBothPriorities();

        Permanent faerie = permanent(player1, "Indigo Faerie");
        // The faerie is already blue; it stays blue and nothing else is added/replaced.
        assertThat(gqs.getEffectiveColors(gd, faerie)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Added blue wears off at end of turn")
    void blueWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new IndigoFaerie());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.BLUE);

        // The floating layer-5 color effect expires at cleanup, leaving only the intrinsic color.
        gd.expireEndOfTurnFloatingEffects();
        bears.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, bears))
                .containsExactly(CardColor.GREEN);
    }

    private Permanent permanent(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
