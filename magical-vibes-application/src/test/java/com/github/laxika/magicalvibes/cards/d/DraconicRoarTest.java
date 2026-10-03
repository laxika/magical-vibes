package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AtarkaEfreet;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicRoar.class, ShivanDragon.class, DragonlordAtarka.class, AtarkaEfreet.class, Twincast.class})
class DraconicRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target creature without the Dragon bonus")
    void dealsDamageWithoutDragonBonus() {
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Revealing a Dragon deals 3 damage to the target creature's controller")
    void revealingDragonDealsBonusDamage() {
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        ShivanDragon revealedDragon = new ShivanDragon();
        harness.setHand(player1, List.of(new DraconicRoar(), revealedDragon));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).contains(revealedDragon);
    }

    @Test
    @DisplayName("Controlling a Dragon as cast deals 3 damage to the target creature's controller")
    void controllingDragonAsCastDealsBonusDamage() {
        ShivanDragon controlledDragon = new ShivanDragon();
        Permanent target = addCreatureReady(player2, new ShivanDragon());
        addCreatureReady(player1, controlledDragon);
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
    }

    @Test
    void revealingAndControllingDragonDoesNotDoubleBonus() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        addCreatureReady(player1, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar(), new DragonlordAtarka()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void lethalCreatureDamageStillDealsControllerDamage() {
        addCreatureReady(player1, new DragonlordAtarka());
        Permanent target = addCreatureReady(player2, new AtarkaEfreet());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Atarka Efreet");
        harness.assertLife(player2, 17);
    }

    @Test
    void losingDragonAfterCastingDoesNotRemoveBonus() {
        Permanent dragon = addCreatureReady(player1, new DragonlordAtarka());
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerGraveyards.get(player1.getId()).add(dragon.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);
    }

    @Test
    void gainingDragonAfterCastingDoesNotGrantBonus() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        addCreatureReady(player1, new DragonlordAtarka());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void illegalCreatureTargetPreventsControllerDamage() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar(), new DragonlordAtarka()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Draconic Roar");
    }

    @Test
    void targetingOwnDragonDamagesItsController() {
        Permanent target = addCreatureReady(player1, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void copyRetainsBonusFromRevealingDragon() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        DraconicRoar roar = new DraconicRoar();
        harness.setHand(player1, List.of(roar, new DragonlordAtarka()));
        harness.setHand(player2, List.of(new Twincast()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, roar.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 17);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player2, 14);
    }

    @Test
    void copyDoesNotRetainBonusFromControllingDragonAsCast() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        addCreatureReady(player1, new DragonlordAtarka());
        DraconicRoar roar = new DraconicRoar();
        harness.setHand(player1, List.of(roar));
        harness.setHand(player2, List.of(new Twincast()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, roar.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertLife(player2, 17);
    }

    @Test
    void cannotRevealNonDragonForAdditionalCost() {
        Permanent target = addCreatureReady(player2, new DragonlordAtarka());
        harness.setHand(player1, List.of(new DraconicRoar(), new AtarkaEfreet()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DraconicRoar()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
