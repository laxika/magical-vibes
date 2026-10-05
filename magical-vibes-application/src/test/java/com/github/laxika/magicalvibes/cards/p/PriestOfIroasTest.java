package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfIroas.class, AngelicChorus.class, GrizzlyBears.class})
class PriestOfIroasTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndDestroysTargetEnchantment() {
        addReadyPriest(player1);
        Permanent target = addReadyEnchantment(player2);
        addActivationMana();

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Priest of Iroas");
        harness.assertInGraveyard(player1, "Priest of Iroas");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void cannotTargetCreature() {
        addReadyPriest(player1);
        Permanent target = addReadyCreature(player2);
        addActivationMana();

        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPriest(com.github.laxika.magicalvibes.model.Player player) {
        Permanent priest = harness.addToBattlefieldAndReturn(player, new PriestOfIroas());
        priest.setSummoningSick(false);
        return priest;
    }

    private Permanent addReadyEnchantment(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new AngelicChorus());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    @Test
    void canActivateWhileSummoningSickAndPaysSacrificeBeforeResolution() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new PriestOfIroas());
        priest.setSummoningSick(true);
        Permanent target = addReadyEnchantment(player2);
        addActivationMana();

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Priest of Iroas");
        harness.assertInGraveyard(player1, "Priest of Iroas");
        harness.assertOnBattlefield(player2, "Angelic Chorus");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void canDestroyOwnEnchantment() {
        addReadyPriest(player1);
        Permanent target = addReadyEnchantment(player1);
        addActivationMana();

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Priest of Iroas");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
    }

    @Test
    void cannotActivateWithoutWhiteMana() {
        addReadyPriest(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Priest of Iroas");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }
    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
