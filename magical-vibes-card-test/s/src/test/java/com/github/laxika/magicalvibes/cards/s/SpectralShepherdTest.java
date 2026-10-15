package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralShepherd.class, ApothecaryGeist.class, HamletCaptain.class})
class SpectralShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns target Spirit you control to its owner's hand")
    void abilityReturnsControlledSpiritToHand() {
        addCreatureReady(player1, new SpectralShepherd());
        Permanent spirit = addCreatureReady(player1, new ApothecaryGeist());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Apothecary Geist");
        harness.assertInHand(player1, "Apothecary Geist");
    }

    @Test
    @DisplayName("Ability can bounce Spectral Shepherd itself")
    void canBounceItself() {
        Permanent shepherd = addCreatureReady(player1, new SpectralShepherd());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, shepherd.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spectral Shepherd");
        harness.assertInHand(player1, "Spectral Shepherd");
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        addCreatureReady(player1, new SpectralShepherd());
        Permanent captain = addCreatureReady(player1, new HamletCaptain());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, captain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Spirit")
    void cannotTargetOpponentsSpirit() {
        addCreatureReady(player1, new SpectralShepherd());
        Permanent spirit = addCreatureReady(player2, new ApothecaryGeist());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spirit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A controlled Spirit returns to its owner's hand even when stolen")
    void returnsStolenSpiritToOwnersHand() {
        addCreatureReady(player1, new SpectralShepherd());
        Permanent spirit = addCreatureReady(player1, new ApothecaryGeist());
        gd.stolenCreatures.put(spirit.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Apothecary Geist");
        harness.assertInHand(player2, "Apothecary Geist");
        harness.assertNotInHand(player1, "Apothecary Geist");
    }

    @Test
    @DisplayName("A Spirit that changes controller before resolution is no longer a legal target")
    void doesNotReturnSpiritAfterControlChanges() {
        addCreatureReady(player1, new SpectralShepherd());
        Permanent spirit = addCreatureReady(player1, new ApothecaryGeist());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, spirit.getId());

        gd.playerBattlefields.get(player1.getId()).remove(spirit);
        gd.playerBattlefields.get(player2.getId()).add(spirit);
        gd.stolenCreatures.put(spirit.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Apothecary Geist");
        harness.assertNotInHand(player1, "Apothecary Geist");
        harness.assertNotInHand(player2, "Apothecary Geist");
    }

    @Test
    @DisplayName("Ability can be activated while Shepherd is tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent shepherd = harness.addToBattlefieldAndReturn(player1, new SpectralShepherd());
        shepherd.setSummoningSick(true);
        shepherd.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, shepherd.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spectral Shepherd");
        harness.assertInHand(player1, "Spectral Shepherd");
    }

    @Test
    @DisplayName("Ability requires blue mana even when two generic mana are available")
    void cannotActivateWithoutBlueMana() {
        Permanent shepherd = addCreatureReady(player1, new SpectralShepherd());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shepherd.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spectral Shepherd");
        harness.assertNotInHand(player1, "Spectral Shepherd");
    }

    @Test
    @DisplayName("An activated ability still resolves after Shepherd returns itself to hand")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent shepherd = addCreatureReady(player1, new SpectralShepherd());
        Permanent spirit = addCreatureReady(player1, new ApothecaryGeist());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.activateAbility(player1, 0, null, shepherd.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Spectral Shepherd");
        harness.assertOnBattlefield(player1, "Apothecary Geist");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Apothecary Geist");
        harness.assertInHand(player1, "Apothecary Geist");
    }
}
