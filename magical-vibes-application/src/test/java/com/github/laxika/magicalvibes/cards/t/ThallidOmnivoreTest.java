package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThallidOmnivore.class, BalothGorger.class, YavimayaSapherd.class})
class ThallidOmnivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addThallidOmnivoreReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Thallid Omnivore");
    }

    @Test
    @DisplayName("Sacrificing a Saproling via ability 0 gives +2/+2 and 2 life")
    void sacrificeSaprolingGivesBoostAndLifeGain() {
        addThallidOmnivoreReady(player1);
        addSaprolingToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertNotOnBattlefield(player1, "Saproling");

        Permanent omnivore = findPermanent(player1, "Thallid Omnivore");
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Sacrificing a non-Saproling creature via ability 1 gives +2/+2 but no life gain")
    void sacrificeNonSaprolingGivesBoostNoLifeGain() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Baloth Gorger");

        Permanent omnivore = findPermanent(player1, "Thallid Omnivore");
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot sacrifice Thallid Omnivore to its own second ability (excludeSelf)")
    void cannotSacrificeItself() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertOnBattlefield(player1, "Thallid Omnivore");
        harness.assertInGraveyard(player1, "Baloth Gorger");
    }

    @Test
    @DisplayName("Ability requires {1} mana to activate")
    void abilityRequiresMana() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new BalothGorger());

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent omnivore = findPermanent(player1, "Thallid Omnivore");
        assertThat(omnivore.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(omnivore.getPowerModifier()).isEqualTo(0);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate both abilities across turns of sacrifice")
    void canActivateBothAbilities() {
        addThallidOmnivoreReady(player1);
        addSaprolingToken();
        harness.addToBattlefield(player1, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Baloth Gorger"));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        Permanent omnivore = findPermanent(player1, "Thallid Omnivore");
        assertThat(omnivore.getPowerModifier()).isEqualTo(4);
        assertThat(omnivore.getToughnessModifier()).isEqualTo(4);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Sacrificing a Saproling through the general creature choice still gains life")
    void generalCreatureSacrificeGainsLifeForSaproling() {
        addThallidOmnivoreReady(player1);
        addSaprolingToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Saproling"));

        harness.assertNotOnBattlefield(player1, "Saproling");
        harness.assertLife(player1, lifeBefore);
        assertThat(findPermanent(player1, "Thallid Omnivore").getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thallid Omnivore").getPowerModifier()).isEqualTo(2);
        assertThat(findPermanent(player1, "Thallid Omnivore").getToughnessModifier()).isEqualTo(2);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @CardUsed({ArcaneAdaptation.class})
    @DisplayName("The source cannot sacrifice itself even when it is a Saproling")
    void saprolingSourceCannotSacrificeItself() {
        addThallidOmnivoreReady(player1);
        harness.setHand(player1, List.of(new ArcaneAdaptation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SAPROLING");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Thallid Omnivore");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addThallidOmnivoreReady(player1);
        harness.addToBattlefield(player2, new BalothGorger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Baloth Gorger");
    }

    @Test
    @CardUsed({CastDown.class})
    @DisplayName("Life gain still resolves when Omnivore is destroyed in response")
    void gainsLifeEvenWhenSourceLeavesBattlefield() {
        addThallidOmnivoreReady(player1);
        addSaprolingToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new CastDown()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Thallid Omnivore"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Thallid Omnivore");
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertLife(player2, 20);
    }

    private void addThallidOmnivoreReady(Player player) {
        harness.addToBattlefield(player, new ThallidOmnivore());
    }

    private void addSaprolingToken() {
        harness.enterBattlefieldAndReturn(player1, new YavimayaSapherd());
        resolveAllTriggers();
    }
}
