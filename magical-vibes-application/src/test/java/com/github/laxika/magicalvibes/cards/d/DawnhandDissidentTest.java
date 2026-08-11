package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DawnhandDissidentTest extends BaseCardTest {

    @Test
    void blightOneSurveilsTheTopCard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, topCard);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(dawnhand.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void blightTwoExilesTheTargetFromAGraveyard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new HillGiant());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, costCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).contains(target);
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void castsOwnedCreatureExiledWithDawnhandByRemovingCounters() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new HillGiant());
        costCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Card exiledCreature = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, exiledCreature.getId(),
                List.of(costCreature.getId(), costCreature.getId(), costCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == exiledCreature);
        assertThat(costCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).isEmpty();
    }

    @Test
    void cannotCastNoncreatureExiledWithDawnhand() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Card noncreature = new GiantGrowth();
        gd.addToExile(player1.getId(), noncreature, dawnhand.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }
}
