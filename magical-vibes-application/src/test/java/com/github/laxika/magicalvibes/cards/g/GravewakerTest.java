package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravewakerTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureFromGraveyardTapped() {
        int gravewakerIndex = addReadyGravewaker();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.activateAbilityWithGraveyardTargets(player1, gravewakerIndex, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetNonCreatureCardInGraveyard() {
        int gravewakerIndex = addReadyGravewaker();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, gravewakerIndex, 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateTwiceWithoutTappingSource() {
        int gravewakerIndex = addReadyGravewaker();
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 14);

        harness.activateAbilityWithGraveyardTargets(player1, gravewakerIndex, 0, List.of(first.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, gravewakerIndex, 0, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gravewaker").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Hill Giant").isTapped()).isTrue();
    }

    private int addReadyGravewaker() {
        harness.addToBattlefield(player1, new Gravewaker());
        Permanent gravewaker = findPermanent(player1, "Gravewaker");
        gravewaker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gd.playerBattlefields.get(player1.getId()).indexOf(gravewaker);
    }
}
