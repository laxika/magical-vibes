package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagicPot.class, GrizzlyBears.class, WrathOfGod.class})
class MagicPotTest extends BaseCardTest {

    @Test
    void createsTreasureWhenItDies() {
        harness.addToBattlefield(player1, new MagicPot());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void exilesTargetCardFromAnyGraveyard() {
        Permanent pot = addReadyPot();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(pot), 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    private Permanent addReadyPot() {
        return addCreatureReady(player1, new MagicPot());
    }

    @Test
    void exilesNoncreatureCardFromOwnGraveyardAndTapsAsCost() {
        Permanent pot = addReadyPot();
        Card target = new WrathOfGod();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(pot), 0, List.of(target.getId()));

        assertThat(pot.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent pot = harness.addToBattlefieldAndReturn(player1, new MagicPot());
        pot.setSummoningSick(true);
        Card target = new MagicPot();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(pot), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pot.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent pot = addReadyPot();
        Card target = new MagicPot();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(pot), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pot.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    void doesNotExileAnotherCardWhenTargetLeavesGraveyard() {
        Permanent first = addReadyPot();
        Permanent second = addReadyPot();
        Card target = new MagicPot();
        Card other = new MagicPot();
        harness.setGraveyard(player2, List.of(target, other));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(first), 0, List.of(target.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(second), 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
