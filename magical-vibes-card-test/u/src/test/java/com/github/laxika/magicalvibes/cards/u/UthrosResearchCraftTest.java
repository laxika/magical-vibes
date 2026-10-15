package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UthrosResearchCraft.class, Forest.class, GrizzlyBears.class, Memnite.class})
class UthrosResearchCraftTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds counters equal to the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent craft = harness.addToBattlefieldAndReturn(player1, new UthrosResearchCraft());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(craft), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(craft.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Twelve charge counters animate the Spacecraft, grant flying, and scale its power")
    void twelveChargeCountersUnlockCreatureAbilities() {
        Permanent craft = harness.addToBattlefieldAndReturn(player1, new UthrosResearchCraft());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        assertThat(gqs.isCreature(gd, craft)).isFalse();

        craft.setCounterCount(CounterType.CHARGE, 12);

        assertThat(gqs.isCreature(gd, craft)).isTrue();
        assertThat(gqs.hasKeyword(gd, craft, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, craft)).isEqualTo(3);
    }

    @Test
    @DisplayName("At three charge counters, casting an artifact draws and adds a charge counter")
    void artifactSpellDrawsAndAddsChargeCounter() {
        Permanent craft = harness.addToBattlefieldAndReturn(player1, new UthrosResearchCraft());
        craft.setCounterCount(CounterType.CHARGE, 3);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Uthros Research Craft"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(craft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("The artifact spell trigger is inactive below three charge counters")
    void artifactSpellDoesNotTriggerBelowThreshold() {
        Permanent craft = harness.addToBattlefieldAndReturn(player1, new UthrosResearchCraft());
        craft.setCounterCount(CounterType.CHARGE, 2);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Uthros Research Craft"));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(craft.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The three-counter trigger does not fire for nonartifact spells")
    void nonartifactSpellDoesNotTrigger() {
        Permanent craft = harness.addToBattlefieldAndReturn(player1, new UthrosResearchCraft());
        craft.setCounterCount(CounterType.CHARGE, 3);
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Uthros Research Craft"));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(craft.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
