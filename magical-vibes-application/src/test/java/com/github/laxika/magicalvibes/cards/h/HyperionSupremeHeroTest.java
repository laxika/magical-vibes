package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HyperionSupremeHero.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class HyperionSupremeHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all but one damage to its controller")
    void protectsController() {
        addCreatureReady(player1, new HyperionSupremeHero());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents all but one damage to a Hero it controls")
    void protectsHero() {
        Permanent hyperion = addCreatureReady(player1, new HyperionSupremeHero());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hyperion.getId());

        assertThat(hyperion.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hyperion, Supreme Hero");
    }

    @Test
    @DisplayName("Does not protect a non-Hero creature")
    void doesNotProtectNonHeroCreature() {
        addCreatureReady(player1, new HyperionSupremeHero());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
