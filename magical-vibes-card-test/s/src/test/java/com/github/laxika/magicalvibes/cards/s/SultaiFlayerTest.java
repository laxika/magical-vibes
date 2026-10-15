package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.c.ChiefOfTheScale;
import com.github.laxika.magicalvibes.cards.d.DragonscaleBoon;
import com.github.laxika.magicalvibes.cards.e.EndHostilities;
import com.github.laxika.magicalvibes.cards.m.MarduHordechief;
import com.github.laxika.magicalvibes.cards.t.Throttle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SultaiFlayer.class, GiantSpider.class, GrizzlyBears.class, Murder.class,
        DragonscaleBoon.class, Throttle.class, EndHostilities.class,
        ChiefOfTheScale.class, MarduHordechief.class})
class SultaiFlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when a creature you control with toughness 4 dies")
    void gainsLifeWhenToughCreatureDies() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Giant Spider"));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not trigger when a creature you control has toughness below 4")
    void doesNotTriggerForLowerToughnessCreature() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains 4 life when Sultai Flayer itself dies")
    void gainsLifeWhenItselfDies() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Sultai Flayer"));

        harness.assertLife(player1, 24);
    }

    @Test
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player2, "Giant Spider"));

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void usesToughnessIncludingCounters() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, bearsId);

        destroy(player1, bearsId);

        harness.assertLife(player1, 24);
    }

    @Test
    void gainsFourLifeRegardlessOfHowHighToughnessIs() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setLife(player1, 20);
        UUID spiderId = harness.getPermanentId(player1, "Giant Spider");
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, spiderId);

        destroy(player1, spiderId);

        harness.assertLife(player1, 24);
    }

    @Test
    void doesNotTriggerWhenItsToughnessIsReducedToZero() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Throttle()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Sultai Flayer"));

        harness.assertInGraveyard(player1, "Sultai Flayer");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void seesOtherCreaturesDyingSimultaneouslyWithIt() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EndHostilities()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castAndResolveSorcery(player1, 0, List.<UUID>of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sultai Flayer");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    void usesToughnessFromStaticBoostImmediatelyBeforeDeath() {
        harness.addToBattlefield(player1, new SultaiFlayer());
        harness.addToBattlefield(player1, new ChiefOfTheScale());
        harness.addToBattlefield(player1, new MarduHordechief());
        harness.setLife(player1, 20);

        destroy(player1, harness.getPermanentId(player1, "Mardu Hordechief"));

        harness.assertInGraveyard(player1, "Mardu Hordechief");
        harness.assertLife(player1, 24);
    }

    private void destroy(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
