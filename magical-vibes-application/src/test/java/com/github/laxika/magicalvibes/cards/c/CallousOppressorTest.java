package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallousOppressor.class, GrizzlyBears.class, HillGiant.class})
class CallousOppressorTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the creature type as Callous Oppressor enters")
    void opponentChoosesCreatureType() {
        harness.setHand(player1, List.of(new CallousOppressor()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BEAR");

        Permanent oppressor = findPermanent(player1, "Callous Oppressor");
        assertThat(oppressor.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The activated ability only targets creatures outside the chosen type")
    void onlyTargetsCreatureOutsideChosenType() {
        Permanent oppressor = addReadyOppressor(player1, CardSubtype.BEAR);
        Permanent bear = addReadyCreature(player2, new GrizzlyBears());
        Permanent giant = addReadyCreature(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chosen type");

        harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
        assertThat(oppressor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control ends when Callous Oppressor untaps")
    void controlEndsWhenSourceUntaps() {
        Permanent oppressor = addReadyOppressor(player1, CardSubtype.BEAR);
        Permanent giant = addReadyCreature(player2, new HillGiant());

        harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(oppressor), null, giant.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oppressor.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(giant);
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
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, untap);
    }
}
