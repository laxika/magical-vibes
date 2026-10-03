package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EaterOfHope.class, SatyrWayfinder.class, SpringleafDrum.class})
class EaterOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants Eater of Hope a regeneration shield")
    void sacrificeAnotherCreatureRegeneratesEaterOfHope() {
        Permanent eater = addCreatureReady(player1, new EaterOfHope());
        addCreatureReady(player1, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eater.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Satyr Wayfinder");
    }

    @Test
    @DisplayName("Regeneration ability cannot sacrifice Eater of Hope itself")
    void regenerationAbilityCannotSacrificeItself() {
        addCreatureReady(player1, new EaterOfHope());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing two other creatures destroys the target creature")
    void sacrificeTwoOtherCreaturesDestroysTarget() {
        addCreatureReady(player1, new EaterOfHope());
        Permanent firstSacrifice = addCreatureReady(player1, new SatyrWayfinder());
        addCreatureReady(player1, new SatyrWayfinder());
        Permanent target = addCreatureReady(player2, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Satyr Wayfinder");
        harness.assertInGraveyard(player2, "Satyr Wayfinder");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Destroy ability cannot target a noncreature permanent")
    void destroyAbilityCannotTargetNoncreature() {
        addCreatureReady(player1, new EaterOfHope());
        addCreatureReady(player1, new SatyrWayfinder());
        addCreatureReady(player1, new SatyrWayfinder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpringleafDrum());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy ability needs two other creatures controlled by its controller")
    void destroyAbilityCannotUseSourceOrOpponentToCompleteCost() {
        addCreatureReady(player1, new EaterOfHope());
        addCreatureReady(player1, new SatyrWayfinder());
        Permanent target = addCreatureReady(player2, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Satyr Wayfinder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regeneration cannot sacrifice an opponent's creature")
    void regenerationCannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new EaterOfHope());
        addCreatureReady(player2, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Satyr Wayfinder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroy ability can target Eater of Hope itself, even with summoning sickness")
    void destroyAbilityCanDestroyItsOwnSource() {
        Permanent eater = harness.addToBattlefieldAndReturn(player1, new EaterOfHope());
        Permanent firstSacrifice = addCreatureReady(player1, new SatyrWayfinder());
        addCreatureReady(player1, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, eater.getId());
        harness.handlePermanentChosen(player1, firstSacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player1, "Eater of Hope");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eater of Hope");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield replaces destruction and taps Eater of Hope only when used")
    void regenerationShieldPreventsDestruction() {
        Permanent eater = addCreatureReady(player1, new EaterOfHope());
        Permanent regenerationCost = addCreatureReady(player1, new SatyrWayfinder());
        Permanent firstDestroyCost = addCreatureReady(player1, new SatyrWayfinder());
        addCreatureReady(player1, new SatyrWayfinder());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, regenerationCost.getId());
        harness.passBothPriorities();

        assertThat(eater.getRegenerationShield()).isEqualTo(1);
        assertThat(eater.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 1, null, eater.getId());
        harness.handlePermanentChosen(player1, firstDestroyCost.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eater of Hope");
        harness.assertNotInGraveyard(player1, "Eater of Hope");
        assertThat(eater.getRegenerationShield()).isZero();
        assertThat(eater.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
