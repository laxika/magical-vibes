package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blustersquall.class, AxebaneStag.class, RubblebackRhino.class, Island.class})
class BlustersquallTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature you don't control")
    void tapsTargetCreature() {
        Permanent target = addCreatureReady(player2, new AxebaneStag());
        Permanent own = addCreatureReady(player1, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(own.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent own = addCreatureReady(player1, new AxebaneStag());
        addCreatureReady(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Overloaded, it taps every creature you don't control and needs no target")
    void overloadTapsEveryCreatureYouDontControl() {
        Permanent first = addCreatureReady(player2, new AxebaneStag());
        Permanent second = addCreatureReady(player2, new AxebaneStag());
        Permanent own = addCreatureReady(player1, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(own.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreatureReady(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalCastCannotTargetHexproofCreature() {
        Permanent rhino = addCreatureReady(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, rhino.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overloadTapsHexproofCreaturesButLeavesLandsUntapped() {
        Permanent rhino = addCreatureReady(player2, new RubblebackRhino());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(rhino.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void overloadCanResolveWithoutOpposingCreatures() {
        Permanent own = addCreatureReady(player1, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(own.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Blustersquall);
    }

    @Test
    void normalCastDoesNotTapTargetThatComesUnderYourControlBeforeResolution() {
        Permanent target = addCreatureReady(player2, new AxebaneStag());
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void overloadIncludesCreaturesEnteringAfterItWasCast() {
        harness.setHand(player1, List.of(new Blustersquall()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castWithOverload(player1, 0);

        Permanent lateCreature = addCreatureReady(player2, new AxebaneStag());
        harness.passBothPriorities();

        assertThat(lateCreature.isTapped()).isTrue();
    }
}