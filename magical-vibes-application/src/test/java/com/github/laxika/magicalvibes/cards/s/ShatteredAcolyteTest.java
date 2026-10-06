package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteredAcolyte.class, IronMyr.class, AngelicChorus.class, GrizzlyBears.class})
class ShatteredAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron Myr");
        harness.assertInGraveyard(player2, "Iron Myr");
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Shattered Acolyte is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Shattered Acolyte");
        harness.assertInGraveyard(player1, "Shattered Acolyte");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);


        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void combatDamageGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ShatteredAcolyte()).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void tappedSummoningSickSourceCanDestroyOwnArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ShatteredAcolyte());
        source.setSummoningSick(true);
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shattered Acolyte");
        harness.assertInGraveyard(player1, "Iron Myr");
        harness.assertNotOnBattlefield(player1, "Iron Myr");
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWithoutManaAndDoesNotSacrificeSource() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shattered Acolyte");
        harness.assertNotInGraveyard(player1, "Shattered Acolyte");
        harness.assertOnBattlefield(player2, "Iron Myr");
    }

    @Test
    void cannotActivateWithoutTargetAndDoesNotSacrificeSource() {
        harness.addToBattlefield(player1, new ShatteredAcolyte());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shattered Acolyte");
        harness.assertNotInGraveyard(player1, "Shattered Acolyte");
    }
}
