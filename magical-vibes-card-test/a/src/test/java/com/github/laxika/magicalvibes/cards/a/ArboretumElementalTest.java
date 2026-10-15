package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CollarTheCulprit;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.s.SureStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArboretumElemental.class, IronshellBeetle.class, CollarTheCulprit.class, SureStrike.class})
class ArboretumElementalTest extends BaseCardTest {

    @Test
    void castsWithoutConvoking() {
        harness.setHand(player1, List.of(new ArboretumElemental()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arboretum Elemental");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void summoningSickGreenCreaturesPayColoredCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        harness.setHand(player1, List.of(new ArboretumElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Arboretum Elemental");
    }

    @Test
    void nineCreaturesPayEntireCostWithoutMana() {
        List<UUID> convokers = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            convokers.add(harness.addToBattlefieldAndReturn(player1, new IronshellBeetle()).getId());
        }
        harness.setHand(player1, List.of(new ArboretumElemental()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), convokers);

        assertThat(findPermanents(player1, "Ironshell Beetle")).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Arboretum Elemental");
    }

    @Test
    void cannotConvokeWithAnAlreadyTappedCreature() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        beetle.tap();
        harness.setHand(player1, List.of(new ArboretumElemental()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(beetle.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Arboretum Elemental");
    }

    @Test
    void cannotConvokeWithOpponentsCreature() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new IronshellBeetle());
        harness.setHand(player1, List.of(new ArboretumElemental()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(beetle.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beetle.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Arboretum Elemental");
    }

    @Test
    void cannotCountTheSameCreatureMoreThanOnceForConvoke() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new IronshellBeetle());
        harness.setHand(player1, List.of(new ArboretumElemental()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), Collections.nCopies(9, beetle.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beetle.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Arboretum Elemental");
    }

    @Test
    void opponentCannotTargetWithRemoval() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new ArboretumElemental());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new CollarTheCulprit()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.assertOnBattlefield(player1, "Arboretum Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanDestroyOwnElemental() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new ArboretumElemental());
        harness.setHand(player1, List.of(new CollarTheCulprit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        harness.assertNotOnBattlefield(player1, "Arboretum Elemental");
        harness.assertInGraveyard(player1, "Arboretum Elemental");
    }

    @Test
    void controllerCanTargetWithCombatTrick() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new ArboretumElemental());
        harness.setHand(player1, List.of(new SureStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Arboretum Elemental");
    }
}
