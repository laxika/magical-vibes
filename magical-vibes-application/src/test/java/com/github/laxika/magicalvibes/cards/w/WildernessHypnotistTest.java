package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildernessHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: red target gets -2/-0 until end of turn")
    void redTargetGetsMinusTwoPower() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new HillGiant());

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.activateAbility(player1, 0, null, giantId);
        harness.passBothPriorities();

        Permanent giant = permanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("{T}: green target is a legal target")
    void greenTargetGetsMinusTwoPower() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        Permanent bears = permanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("-2/-0 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new HillGiant());

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.activateAbility(player1, 0, null, giantId);
        harness.passBothPriorities();

        Permanent giant = permanent(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);

        gd.expireEndOfTurnFloatingEffects();
        giant.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a blue creature")
    void cannotTargetBlueCreature() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new FugitiveWizard());

        UUID wizardId = harness.getPermanentId(player2, "Fugitive Wizard");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wizardId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent permanent(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
