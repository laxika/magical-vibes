package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VividGrove;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Restore.class, Forest.class, GrizzlyBears.class, VividGrove.class})
class RestoreTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land from your graveyard to the battlefield")
    void returnsLandFromOwnGraveyard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Returns a target land from an opponent's graveyard under your control")
    void returnsLandFromOpponentsGraveyardUnderYourControl() {
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland card in a graveyard")
    void cannotTargetNonlandCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a nonbasic land and preserves its entry behavior")
    void returnsNonbasicLandWithItsEntryBehavior() {
        Card land = new VividGrove();
        harness.setGraveyard(player2, List.of(land));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Vivid Grove");
        harness.assertNotOnBattlefield(player2, "Vivid Grove");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(land.getId());
                    assertThat(permanent.isTapped()).isTrue();
                    assertThat(permanent.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Does not return another land when the target leaves the graveyard")
    void doesNotReturnAnotherLandWhenTargetLeavesGraveyard() {
        Card target = new Forest();
        Card otherLand = new VividGrove();
        harness.setGraveyard(player2, List.of(target, otherLand));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.getGameData().playerGraveyards.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Vivid Grove");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId())).contains(target);
        harness.assertInGraveyard(player1, "Restore");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land card in a hand")
    void cannotTargetLandOutsideGraveyard() {
        Card land = new Forest();
        harness.setHand(player2, List.of(land));
        harness.setHand(player1, List.of(new Restore()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
