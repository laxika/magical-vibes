package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EncroachingWastes.class, Forest.class, Mutavault.class, ElvishMystic.class})
class EncroachingWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana with first ability")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new EncroachingWastes());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Activating destroy ability sacrifices Encroaching Wastes and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        harness.addToBattlefield(player2, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player2, "Mutavault");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Encroaching Wastes");
        harness.assertInGraveyard(player1, "Encroaching Wastes");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving destroys the target nonbasic land")
    void resolvingDestroysNonbasicLand() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        harness.addToBattlefield(player2, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player2, "Mutavault");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mutavault");
        harness.assertInGraveyard(player2, "Mutavault");
    }

    @Test
    @DisplayName("Can target own nonbasic land")
    void canTargetOwnNonbasicLand() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        harness.addToBattlefield(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player1, "Mutavault");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mutavault");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        harness.addToBattlefield(player2, new Mutavault());
        UUID targetId = harness.getPermanentId(player2, "Mutavault");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefieldAndReturn(player1, new EncroachingWastes()).tap();
        harness.addToBattlefield(player2, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player2, "Mutavault");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target itself and still pays costs before the ability loses its target")
    void canTargetItself() {
        UUID sourceId = harness.addToBattlefieldAndReturn(player1, new EncroachingWastes()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, sourceId);

        harness.assertInGraveyard(player1, "Encroaching Wastes");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Generic activation cost can be paid with colored mana")
    void canPayWithColoredMana() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mutavault()).getId();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mutavault");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot target a nonland creature or spend costs for an illegal target")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new EncroachingWastes());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ElvishMystic()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Encroaching Wastes");
        harness.assertNotInGraveyard(player1, "Encroaching Wastes");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
