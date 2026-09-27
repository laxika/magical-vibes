package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BetrayalOfFlesh.class, AlphaMyr.class, Plains.class})
class BetrayalOfFleshTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInGraveyard(player2, "Alpha Myr");
    }

    @Test
    void cannotDestroyNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reanimatesTargetCreatureCard() {
        AlphaMyr deadCreature = new AlphaMyr();
        harness.setGraveyard(player1, new ArrayList<>(List.of(deadCreature)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, deadCreature.getId(), List.of());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Alpha Myr");
    }

    @Test
    void cannotReanimateNonCreatureCard() {
        Plains land = new Plains();
        harness.setGraveyard(player1, new ArrayList<>(List.of(land)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, land.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReanimateCreatureFromOpponentsGraveyard() {
        AlphaMyr opponentCreature = new AlphaMyr();
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCreature)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, opponentCreature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entwineSacrificesThreeLandsAndResolvesBothModes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent thirdLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        AlphaMyr deadCreature = new AlphaMyr();
        harness.setGraveyard(player1, new ArrayList<>(List.of(deadCreature)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                deadCreature.getId(), null, List.of(target.getId()), List.of(), false, null, null,
                List.of(), null, null, false, null, List.of(), null,
                List.of(firstLand.getId(), secondLand.getId(), thirdLand.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertOnBattlefield(player1, "Alpha Myr");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstLand, secondLand, thirdLand);
    }

    @Test
    void entwineRequiresThreeLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        AlphaMyr deadCreature = new AlphaMyr();
        harness.setGraveyard(player1, new ArrayList<>(List.of(deadCreature)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                deadCreature.getId(), null, List.of(target.getId()), List.of(), false, null, null,
                List.of(), null, null, false, null, List.of(), null,
                List.of(firstLand.getId(), secondLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entwineRejectsNonlandSacrifice() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        AlphaMyr deadCreature = new AlphaMyr();
        harness.setGraveyard(player1, new ArrayList<>(List.of(deadCreature)));
        harness.setHand(player1, List.of(new BetrayalOfFlesh()));
        addMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                deadCreature.getId(), null, List.of(target.getId()), List.of(), false, null, null,
                List.of(), null, null, false, null, List.of(), null,
                List.of(firstLand.getId(), secondLand.getId(), nonland.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
