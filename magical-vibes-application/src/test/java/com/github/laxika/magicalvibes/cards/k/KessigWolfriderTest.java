package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.v.VoldarenEpicure;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KessigWolfrider.class, VoldarenEpicure.class, Mountain.class})
class KessigWolfriderTest extends BaseCardTest {

    @Test
    void exilesThreeGraveyardCardsAndCreatesWolfToken() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new VoldarenEpicure(), new VoldarenEpicure(), new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wolfrider.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getPower()).isEqualTo(3);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    void cannotActivateWithoutThreeCardsInGraveyard() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new VoldarenEpicure(), new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wolfrider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    void paysCostsBeforeResolutionAndAbilitySurvivesSourceLeaving() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain(), new VoldarenEpicure()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(wolfrider.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(countPermanents(player1, "Wolf")).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(wolfrider);
        gd.playerGraveyards.get(player1.getId()).add(wolfrider.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(countPermanents(player2, "Wolf")).isZero();
    }

    @Test
    void choosesThreeCardsWhenMoreAreAvailable() {
        addReadyWolfrider(player1);
        Mountain retained = new Mountain();
        Mountain first = new Mountain();
        VoldarenEpicure second = new VoldarenEpicure();
        KessigWolfrider third = new KessigWolfrider();
        harness.setGraveyard(player1, List.of(retained, first, second, third));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(retained);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    void cannotUseOpponentsGraveyardToPayCost() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolfrider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent wolfrider = addReadyWolfrider(player1);
        wolfrider.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolfrider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent wolfrider = addReadyWolfrider(player1);
        wolfrider.setTapped(true);
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void cannotPayRedManaRequirementWithOnlyColorlessMana() {
        Permanent wolfrider = addReadyWolfrider(player1);
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wolfrider.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new KessigWolfrider());
        addCreatureReady(player2, new KessigWolfrider());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new KessigWolfrider());
        Permanent first = addCreatureReady(player2, new KessigWolfrider());
        Permanent second = addCreatureReady(player2, new KessigWolfrider());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent addReadyWolfrider(Player player) {
        Permanent wolfrider = addCreatureReady(player, new KessigWolfrider());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return wolfrider;
    }
}
