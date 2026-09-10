package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.m.MoggBombers;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.cards.w.WallOfTears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConstantMists.class,
        ManaLeak.class,
        MoggBombers.class,
        VolrathsStronghold.class,
        WallOfTears.class,
        YouthfulKnight.class
})
class ConstantMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage when it resolves")
    void preventsAllCombatDamage() {
        harness.castFromHand(player1, new ConstantMists(), "{1}{G}");
        harness.passBothPriorities();

        addCreatureReady(player2, new YouthfulKnight());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Constant Mists");
    }

    @Test
    @DisplayName("Does not prevent noncombat damage this turn")
    void doesNotPreventNoncombatDamage() {
        harness.castFromHand(player1, new ConstantMists(), "{1}{G}");
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new MoggBombers());
        harness.castFromHand(player1, new YouthfulKnight(), "{1}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Mogg Bombers");
    }

    @Test
    @DisplayName("Paying buyback sacrifices a land and returns Constant Mists to hand")
    void buybackSacrificesLandAndReturnsToHand() {
        var land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setHand(player1, List.of(new ConstantMists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrificeAndBuyback(player1, 0, null, land.getId());
        assertThat(findPermanents(player1, "Volrath's Stronghold")).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Constant Mists");
        harness.assertNotInGraveyard(player1, "Constant Mists");
    }

    @Test
    @DisplayName("Buyback cannot sacrifice a nonland permanent")
    void buybackRequiresLand() {
        var nonland = harness.addToBattlefieldAndReturn(player1, new WallOfTears());
        harness.setHand(player1, List.of(new ConstantMists()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrificeAndBuyback(
                player1, 0, null, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Constant Mists");
        harness.assertOnBattlefield(player1, "Wall of Tears");
    }

    @Test
    @DisplayName("Countered buyback spell goes to its graveyard")
    void counteredBuybackSpellGoesToGraveyard() {
        var land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        ConstantMists mists = new ConstantMists();
        harness.setHand(player1, List.of(mists));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithSacrificeAndBuyback(player1, 0, null, land.getId());

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, mists.getId());

        harness.assertInGraveyard(player1, "Constant Mists");
        harness.assertNotInHand(player1, "Constant Mists");
    }
}
