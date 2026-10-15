package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostileRealm.class, Mutavault.class, ElvishWarrior.class})
class HostileRealmTest extends BaseCardTest {

    private Permanent setUpEnchantedMutavault() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HostileRealm());
        aura.setAttachedTo(mutavault.getId());
        return mutavault;
    }

    @Test
    @DisplayName("Enchanted land's granted ability makes target creature can't block")
    void grantedAbilityMakesTargetCantBlock() {
        setUpEnchantedMutavault();
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        // Mutavault's own ability is index 0; Hostile Realm's granted ability is index 1.
        harness.activateAbility(player1, 0, 1, null, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Activating the granted ability taps the enchanted land")
    void grantedAbilityTapsLand() {
        Permanent mutavault = setUpEnchantedMutavault();
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        harness.activateAbility(player1, 0, 1, null, warrior.getId());

        assertThat(mutavault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability goes away when aura leaves the battlefield")
    void grantedAbilityRemovedWhenAuraLeaves() {
        Permanent mutavault = setUpEnchantedMutavault();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Hostile Realm"));

        assertThat(gqs.computeStaticBonus(gd, mutavault).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Can enchant a land")
    void canEnchantLand() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player1, new Mutavault());
        harness.setHand(player1, List.of(new HostileRealm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, mutavault.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HostileRealm
                        && mutavault.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new HostileRealm()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Granted ability cannot target a land")
    void grantedAbilityCannotTargetLand() {
        Permanent mutavault = setUpEnchantedMutavault();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mutavault.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(mutavault.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's enchanted land grants its ability to that opponent")
    void opponentControlsGrantedAbility() {
        Permanent mutavault = harness.addToBattlefieldAndReturn(player2, new Mutavault());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new HostileRealm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, mutavault.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, 1, null, warrior.getId());
        harness.passBothPriorities();

        assertThat(mutavault.isTapped()).isTrue();
        assertThat(warrior.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("An activated ability still resolves after Hostile Realm leaves")
    void abilityResolvesAfterAuraLeaves() {
        setUpEnchantedMutavault();
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        harness.activateAbility(player1, 0, 1, null, warrior.getId());
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof HostileRealm);
        harness.passBothPriorities();

        assertThat(warrior.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void blockingRestrictionExpires() {
        setUpEnchantedMutavault();
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.activateAbility(player1, 0, 1, null, warrior.getId());
        harness.passBothPriorities();
        assertThat(warrior.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(warrior.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The granted ability can target a creature its controller controls")
    void grantedAbilityCanTargetOwnCreature() {
        setUpEnchantedMutavault();
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        harness.activateAbility(player1, 0, 1, null, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.isCantBlockThisTurn()).isTrue();
    }
}
