package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeledrosWitherbloomTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest during each upkeep, and its death gains 1 life")
    void createsPestDuringEachUpkeepAndPestDeathGainsLife() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        Permanent pest = findPest(player1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Pays 10 life and untaps only lands controlled by Beledros's controller")
    void paysLifeAndUntapsControlledLands() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        bears.tap();
        opposingForest.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(forest.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(opposingForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability can only be activated once each turn")
    void untapAbilityOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new BeledrosWitherbloom());
        harness.setLife(player1, 30);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private Permanent findPest(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .findFirst()
                .orElseThrow();
    }
}
