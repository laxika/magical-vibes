package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.e.EnormousEnergyBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({NahiriTheLithomancer.class, Bonesplitter.class, GrizzlyBears.class, LeoninScimitar.class,
        EnormousEnergyBlade.class})
class NahiriTheLithomancerTest extends BaseCardTest {

    @Test
    void createsKorSoldierAndMayAttachControlledEquipment() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Kor Soldier");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bonesplitter.getId());

        assertThat(bonesplitter.getAttachedTo()).isEqualTo(token.getId());
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void putsEquipmentFromHandOrGraveyardOntoTheBattlefield() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        Bonesplitter fromHand = new Bonesplitter();
        LeoninScimitar fromGraveyard = new LeoninScimitar();
        harness.setHand(player1, List.of(fromHand));
        harness.setGraveyard(player1, List.of(fromGraveyard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(fromHand.getId(), fromGraveyard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(fromGraveyard.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(fromGraveyard.getId()));
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void ultimateCreatesIndestructibleStoneforgedBladeWithZeroEquip() {
        Permanent nahiri = addReadyNahiri(player1, 10);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent blade = findPermanent(player1, "Stoneforged Blade");
        assertThat(blade.getCard().isToken()).isTrue();
        assertThat(blade.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(blade.getCard().getKeywords()).contains(Keyword.INDESTRUCTIBLE);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int bladeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blade);
        harness.activateAbility(player1, bladeIndex, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyNahiri(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NahiriTheLithomancer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    void mayDeclineAttachingEquipment() {
        addReadyNahiri(player1, 3);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Kor Soldier"))).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void createsTokenWithoutOfferingOpponentsEquipment() {
        addReadyNahiri(player1, 3);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Soldier");
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayPutEquipmentFromHandWithoutPayingItsCost() {
        addReadyNahiri(player1, 3);
        Bonesplitter equipment = new Bonesplitter();
        GrizzlyBears nonEquipment = new GrizzlyBears();
        harness.setHand(player1, List.of(equipment, nonEquipment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class)
                .validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        harness.assertOnBattlefield(player1, "Bonesplitter");
        harness.assertNotInHand(player1, "Bonesplitter");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Bonesplitter").getAttachedTo()).isNull();
    }

    @Test
    void mayDeclinePuttingEquipmentOntoBattlefield() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        harness.setHand(player1, List.of(new Bonesplitter()));
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInHand(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attachingEquipmentTriggersItsAttachmentAbility() {
        addReadyNahiri(player1, 3);
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new EnormousEnergyBlade());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Kor Soldier");
        harness.handlePermanentChosen(player1, blade.getId());
        resolveAllTriggers();

        assertThat(blade.getAttachedTo()).isEqualTo(token.getId());
        assertThat(token.isTapped()).isTrue();
    }
}
