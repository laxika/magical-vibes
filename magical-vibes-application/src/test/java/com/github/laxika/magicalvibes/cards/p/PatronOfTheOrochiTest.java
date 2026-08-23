package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatronOfTheOrochiTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all Forests and green creatures")
    void untapsForestsAndGreenCreatures() {
        Permanent patron = addCreatureReady(player1, new PatronOfTheOrochi());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingGreenCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent nongreenCreature = addCreatureReady(player2, new GoblinPiker());

        forest.tap();
        opposingForest.tap();
        greenCreature.tap();
        opposingGreenCreature.tap();
        nongreenCreature.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(patron.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(opposingForest.isTapped()).isFalse();
        assertThat(greenCreature.isTapped()).isFalse();
        assertThat(opposingGreenCreature.isTapped()).isFalse();
        assertThat(nongreenCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability can only be activated once each turn")
    void untapAbilityOnlyOnceEachTurn() {
        addCreatureReady(player1, new PatronOfTheOrochi());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Offering sacrifices a Snake and reduces the mana cost")
    void offeringSacrificesSnakeAndReducesManaCost() {
        Permanent snake = addCreatureReady(player1, new SakuraTribeElder());
        harness.setHand(player1, List.of(new PatronOfTheOrochi()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(snake.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patron of the Orochi");
        harness.assertNotOnBattlefield(player1, "Sakura-Tribe Elder");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
