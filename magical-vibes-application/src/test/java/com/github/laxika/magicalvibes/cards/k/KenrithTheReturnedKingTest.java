package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KenrithTheReturnedKing.class, GrizzlyBears.class, HolyDay.class})
class KenrithTheReturnedKingTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives all creatures trample and haste until end of turn")
    void allCreaturesGainTrampleAndHasteUntilEndOfTurn() {
        Permanent kenrith = addReadyKenrith();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Green ability puts a +1/+1 counter on a target creature")
    void putsCounterOnTargetCreature() {
        addReadyKenrith();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("White ability makes the target player gain five life")
    void targetPlayerGainsFiveLife() {
        addReadyKenrith();
        harness.setLife(player2, 10);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Blue ability makes the target player draw a card")
    void targetPlayerDrawsCard() {
        addReadyKenrith();
        Card draw = new GrizzlyBears();
        harness.setLibrary(player2, List.of(draw));
        int handBefore = gd.playerHands.get(player2.getId()).size();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).contains(draw);
    }

    @Test
    @DisplayName("Black ability returns a target creature from any graveyard under its owner's control")
    void returnsTargetCreatureFromAnyGraveyardUnderItsOwnersControl() {
        addReadyKenrith();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 4, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("Black ability rejects a noncreature graveyard target")
    void blackAbilityRejectsNoncreatureTarget() {
        addReadyKenrith();
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 4, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player-targeting abilities reject permanent targets")
    void playerTargetingAbilitiesRejectPermanentTargets() {
        addReadyKenrith();
        Permanent permanent = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKenrith() {
        return addCreatureReady(player1, new KenrithTheReturnedKing());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
