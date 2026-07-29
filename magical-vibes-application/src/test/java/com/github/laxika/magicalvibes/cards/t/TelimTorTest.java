package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MtendaHerder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TelimTorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Telim'Tor gives +1/+1 to every attacking creature with flanking")
    void boostsAttackingFlankers() {
        addCreatureReady(player1, new TelimTor());
        addCreatureReady(player1, new MtendaHerder());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        Permanent telimTor = findPermanent(player1, "Telim'Tor");
        Permanent herder = findPermanent(player1, "Mtenda Herder");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, telimTor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, telimTor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A flanking creature that stays home is not boosted")
    void doesNotBoostNonAttackingFlankers() {
        addCreatureReady(player1, new TelimTor());
        addCreatureReady(player1, new MtendaHerder());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent herder = findPermanent(player1, "Mtenda Herder");
        assertThat(gqs.getEffectivePower(gd, herder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, herder)).isEqualTo(1);
    }
}
