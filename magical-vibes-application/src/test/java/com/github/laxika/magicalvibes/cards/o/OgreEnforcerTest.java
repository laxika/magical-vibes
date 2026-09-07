package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.h.HearthCharm;
import com.github.laxika.magicalvibes.cards.l.LightningCloud;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreEnforcer.class, Fireblast.class, HearthCharm.class, LightningCloud.class,
        Pariah.class, Tremor.class})
class OgreEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Survives lethal damage combined from multiple Tremor sources")
    void survivesDamageFromMultipleSources() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Tremor(), new Tremor(), new Tremor(), new Tremor()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));
        assertThat(enforcer.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Dies when a single source deals lethal damage")
    void diesToSingleSourceLethalDamage() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, enforcer.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Still dies to 0 toughness")
    void diesToZeroToughness() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.setToughnessModifier(-4);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Survives when two sources each mark half of lethal damage")
    void survivesSplitMarkedDamageFromTwoSources() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Dies when one source deals lethal damage in separate events")
    void diesWhenOneSourceDealsLethalDamageInSeparateEvents() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        UUID sourceId = UUID.randomUUID();

        enforcer.addMarkedDamage(sourceId, 2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));

        enforcer.addMarkedDamage(sourceId, 2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    @Test
    @DisplayName("Redirected damage from one source is lethal across separate events")
    void diesWhenRedirectedDamageFromOneSourceIsLethalInSeparateEvents() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        Permanent pariah = harness.addToBattlefieldAndReturn(player2, new Pariah());
        pariah.setAttachedTo(enforcer.getId());
        harness.addToBattlefield(player1, new LightningCloud());
        harness.setHand(player1, List.of(
                new HearthCharm(), new HearthCharm(), new HearthCharm(), new HearthCharm()));
        harness.addMana(player1, ManaColor.RED, 8);

        dealOneLightningCloudDamageToPlayer();
        dealOneLightningCloudDamageToPlayer();
        dealOneLightningCloudDamageToPlayer();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(enforcer.getId()));

        dealOneLightningCloudDamageToPlayer();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enforcer.getId()));
    }

    private void dealOneLightningCloudDamageToPlayer() {
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
