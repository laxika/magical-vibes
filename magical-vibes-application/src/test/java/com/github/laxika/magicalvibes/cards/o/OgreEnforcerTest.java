package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.h.HearthCharm;
import com.github.laxika.magicalvibes.cards.l.LightningCloud;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreEnforcer.class, Fireblast.class, HearthCharm.class, LightningCloud.class,
        Nekrataal.class, Pariah.class, Tremor.class})
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

        harness.assertOnBattlefield(player2, "Ogre Enforcer");
        assertThat(enforcer.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Dies when a single source deals lethal damage")
    void diesToSingleSourceLethalDamage() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, enforcer.getId());

        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Still dies to 0 toughness")
    void diesToZeroToughness() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.setToughnessModifier(-4);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Survives when two sources each mark half of lethal damage")
    void survivesSplitMarkedDamageFromTwoSources() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);
        enforcer.addMarkedDamage(UUID.randomUUID(), 2);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Dies when one source deals lethal damage in separate events")
    void diesWhenOneSourceDealsLethalDamageInSeparateEvents() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        UUID sourceId = UUID.randomUUID();

        enforcer.addMarkedDamage(sourceId, 2);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Ogre Enforcer");

        enforcer.addMarkedDamage(sourceId, 2);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
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
        harness.assertOnBattlefield(player2, "Ogre Enforcer");

        dealOneLightningCloudDamageToPlayer();

        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Ordinary destruction still destroys Ogre Enforcer")
    void diesToDestroyEffect() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enforcer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
        harness.assertInGraveyard(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Uses current toughness when deciding whether a source dealt lethal damage")
    void diesWhenToughnessFallsToDamageFromOneSource() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        harness.setHand(player1, List.of(new Tremor(), new Tremor()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        enforcer.setToughnessModifier(-2);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Ogre Enforcer");

        enforcer.setToughnessModifier(-3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
    }

    @Test
    @DisplayName("Four damage from one source is not lethal while toughness is five")
    void survivesSingleSourceDamageBelowModifiedToughness() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        enforcer.setToughnessModifier(1);
        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, enforcer.getId());

        harness.assertOnBattlefield(player2, "Ogre Enforcer");
        assertThat(enforcer.getMarkedDamage()).isEqualTo(4);

        enforcer.setToughnessModifier(0);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
    }

    private void dealOneLightningCloudDamageToPlayer() {
        harness.castModalInstant(player1, 0, 1, List.of());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Lethal combat damage from a single creature destroys Ogre Enforcer")
    void diesToSingleSourceCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OgreEnforcer());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OgreEnforcer());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertNotOnBattlefield(player1, "Ogre Enforcer");
        harness.assertNotOnBattlefield(player2, "Ogre Enforcer");
        harness.assertInGraveyard(player1, "Ogre Enforcer");
        harness.assertInGraveyard(player2, "Ogre Enforcer");
    }
}
