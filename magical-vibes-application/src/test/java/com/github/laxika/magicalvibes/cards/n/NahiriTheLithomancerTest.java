package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NahiriTheLithomancer.class, Bonesplitter.class, GrizzlyBears.class})
class NahiriTheLithomancerTest extends BaseCardTest {

    @Test
    void createsKorSoldierAndMayAttachEquipment() {
        Permanent nahiri = addReadyNahiri(3);
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Kor Soldier");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bonesplitter.getId());

        assertThat(bonesplitter.getAttachedTo()).isEqualTo(token.getId());
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void putsEquipmentFromHandOntoTheBattlefield() {
        Permanent nahiri = addReadyNahiri(3);
        Card equipment = new Bonesplitter();
        harness.setHand(player1, List.of(equipment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(findPermanent(player1, "Bonesplitter")).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(equipment);
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void putsEquipmentFromGraveyardOntoTheBattlefield() {
        Permanent nahiri = addReadyNahiri(3);
        Card equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(equipment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(findPermanent(player1, "Bonesplitter")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(equipment);
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void createsIndestructibleStoneforgedBladeThatBoostsAndGivesDoubleStrike() {
        addReadyNahiri(10);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Stoneforged Blade");
        assertThat(gqs.hasKeyword(gd, blade, Keyword.INDESTRUCTIBLE)).isTrue();

        int bladeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blade);
        harness.activateAbility(player1, bladeIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent addReadyNahiri(int loyalty) {
        Permanent permanent = new Permanent(new NahiriTheLithomancer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
