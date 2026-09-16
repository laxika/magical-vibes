package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallousOppressor.class, GlorySeeker.class, BarkhideMauler.class})
class CallousOppressorTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the creature type as Callous Oppressor enters")
    void opponentChoosesCreatureType() {
        harness.setHand(player1, List.of(new CallousOppressor()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "SOLDIER");

        Permanent oppressor = findPermanent(player1, "Callous Oppressor");
        assertThat(oppressor.getChosenSubtype()).isEqualTo(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("The activated ability only targets creatures outside the chosen type")
    void onlyTargetsCreatureOutsideChosenType() {
        Permanent oppressor = addReadyOppressor(player1, CardSubtype.SOLDIER);
        Permanent soldier = addReadyCreature(player2, new GlorySeeker());
        Permanent beast = addReadyCreature(player2, new BarkhideMauler());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chosen type");

        harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, beast.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(beast);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(beast);
        assertThat(oppressor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control ends when Callous Oppressor untaps")
    void controlEndsWhenSourceUntaps() {
        Permanent oppressor = addReadyOppressor(player1, CardSubtype.SOLDIER);
        Permanent beast = addReadyCreature(player2, new BarkhideMauler());

        harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, beast.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oppressor.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(beast);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beast);
    }

    @Test
    @DisplayName("Control persists when Callous Oppressor remains tapped")
    void controlPersistsWhenSourceRemainsTapped() {
        Permanent oppressor = addReadyOppressor(player1, CardSubtype.SOLDIER);
        Permanent beast = addReadyCreature(player2, new BarkhideMauler());

        harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, beast.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(oppressor.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(beast);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(beast);
    }

    private Permanent addReadyOppressor(Player player, CardSubtype chosenSubtype) {
        Permanent oppressor = harness.addToBattlefieldAndReturn(player, new CallousOppressor());
        oppressor.setChosenSubtype(chosenSubtype);
        oppressor.setSummoningSick(false);
        return oppressor;
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean untap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, untap);
    }
}
