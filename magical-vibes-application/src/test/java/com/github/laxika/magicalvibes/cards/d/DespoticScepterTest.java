package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DespoticScepter.class, DrudgeSkeletons.class, Forest.class})
class DespoticScepterTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a permanent its controller owns")
    void destroysOwnPermanent() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Destroyed permanent cannot be regenerated")
    void destroyedPermanentCannotRegenerate() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, skeletons.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("Ability taps the Scepter and cannot be activated again while tapped")
    void abilityRequiresTapping() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        assertThat(scepter.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permanent is already tapped");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Can target and destroy itself")
    void canDestroyItself() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new DespoticScepter());

        harness.activateAbility(player1, 0, null, scepter.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Despotic Scepter");
        harness.assertInGraveyard(player1, "Despotic Scepter");
    }

    @Test
    @DisplayName("Destroys an owned permanent controlled by an opponent")
    void destroysOwnedPermanentUnderOpponentControl() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.stolenCreatures.put(forest.getId(), player1.getId());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a controlled permanent owned by an opponent")
    void cannotTargetBorrowedPermanent() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(forest.getId(), player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("A change of target controller does not invalidate ownership targeting")
    void targetRemainsLegalAfterControlChanges() {
        harness.addToBattlefield(player1, new DespoticScepter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.activateAbility(player1, 0, null, forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        gd.stolenCreatures.put(forest.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }
}
