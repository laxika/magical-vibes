package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerOfCalamities.class, AlphaTyrranax.class})
class TowerOfCalamitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 12 damage to target creature when ability resolves")
    void deals12DamageToTargetCreature() {
        addCreatureReady(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("Deals 12 damage but creature survives if toughness is high enough")
    void creatureSurvivesWithHighToughness() {
        addCreatureReady(player1, new TowerOfCalamities());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 7); // 1 short
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps when ability is activated")
    void tapsOnActivation() {
        addCreatureReady(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent tower = findPermanent(player1, "Tower of Calamities");
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        addCreatureReady(player1, new TowerOfCalamities());
        harness.addToBattlefield(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Marks exactly 12 damage on a surviving creature")
    void marksExactlyTwelveDamage() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaTyrranax());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(12);
        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("A tapped Tower cannot activate")
    void cannotActivateWhileTapped() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCalamities());
        tower.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature controlled by the ability controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlphaTyrranax());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpha Tyrranax");
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TowerOfCalamities());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability resolves even when the Tower leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new TowerOfCalamities());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaTyrranax());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Tyrranax");
    }
}
