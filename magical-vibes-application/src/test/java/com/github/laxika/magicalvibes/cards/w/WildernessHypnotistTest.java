package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.IndigoFaerie;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.r.RuggedPrairie;
import com.github.laxika.magicalvibes.cards.s.StigmaLasher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildernessHypnotist.class, StigmaLasher.class, NettleSentinel.class,
        IndigoFaerie.class, RuggedPrairie.class})
class WildernessHypnotistTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: red target gets -2/-0 until end of turn")
    void redTargetGetsMinusTwoPower() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new StigmaLasher());

        UUID targetId = harness.getPermanentId(player2, "Stigma Lasher");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Stigma Lasher");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("{T}: green target is a legal target")
    void greenTargetGetsMinusTwoPower() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new NettleSentinel());

        UUID targetId = harness.getPermanentId(player2, "Nettle Sentinel");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Nettle Sentinel");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
    }

    @Test
    @DisplayName("-2/-0 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new StigmaLasher());

        UUID targetId = harness.getPermanentId(player2, "Stigma Lasher");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Stigma Lasher");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a blue creature")
    void cannotTargetBlueCreature() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new IndigoFaerie());

        UUID faerieId = harness.getPermanentId(player2, "Indigo Faerie");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faerieId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new WildernessHypnotist());
        harness.addToBattlefield(player2, new RuggedPrairie());

        UUID landId = harness.getPermanentId(player2, "Rugged Prairie");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
