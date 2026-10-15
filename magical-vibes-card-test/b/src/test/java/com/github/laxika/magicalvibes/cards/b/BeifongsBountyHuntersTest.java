package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.e.EarthbendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeifongsBountyHunters.class, EarthbendingLesson.class, Forest.class,
        GrizzlyBears.class, Murder.class, GloriousAnthem.class, Demystify.class})
class BeifongsBountyHuntersTest extends BaseCardTest {

    @Test
    @DisplayName("Earthbends a land by the last-known power of another nonland creature that dies")
    void earthbendsByDyingCreaturePower() {
        harness.addToBattlefield(player1, new BeifongsBountyHunters());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroy(creature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Earthbends when Beifong's Bounty Hunters dies")
    void earthbendsWhenItDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BeifongsBountyHunters());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        destroy(source);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when an earthbended land dies")
    void doesNotTriggerForLandCreature() {
        harness.addToBattlefield(player1, new BeifongsBountyHunters());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new EarthbendingLesson()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0, land.getId());

        destroy(land);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.getCard().getId()).isEqualTo(land.getCard().getId());
        assertThat(gqs.isLand(gd, returnedLand)).isTrue();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
    }

    @Test
    @DisplayName("Own death uses last-known power even if an anthem leaves before resolution")
    void ownDeathPreservesStaticPowerBonus() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BeifongsBountyHunters());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        destroy(source);
        harness.handlePermanentChosen(player1, land.getId());

        harness.setHand(player1, List.of(new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, anthem.getId());
        resolveAllTriggers();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's nonland creature dying does not trigger earthbend")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new BeifongsBountyHunters());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroy(creature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    @DisplayName("The chosen land returns tapped without counters after the source has died")
    void earthbendedLandReturnsAfterSourceDeath() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BeifongsBountyHunters());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        destroy(source);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        destroy(land);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getCard().getId()).isEqualTo(land.getCard().getId());
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
    }

    private void destroy(Permanent permanent) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
    }
}
