package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnTriumphant.class, CentaurCourser.class, SerraAngel.class, TitanicGrowth.class})
class ReturnTriumphantTest extends BaseCardTest {

    @Test
    void returnsCreatureWithManaValueThreeOrLessAndAttachesYoungHeroRole() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        UUID centaurId = centaur.getId();
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(centaurId));

        Permanent returned = findPermanent(player1, "Centaur Courser");
        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(centaurId));
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(returned.getId());
    }

    @Test
    void youngHeroPutsCounterOnReturnedCreatureWhenItAttacks() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(centaur.getId()));
        Permanent returned = findPermanent(player1, "Centaur Courser");
        returned.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(returned)));
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rejectsCreatureCardWithManaValueGreaterThanThree() {
        SerraAngel angel = new SerraAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(angel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureCardInOpponentsGraveyard() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player2, List.of(centaur));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(centaur.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureCardWithLowManaValue() {
        TitanicGrowth growth = new TitanicGrowth();
        harness.setGraveyard(player1, List.of(growth));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(growth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsNoRoleWhenTargetLeavesGraveyardBeforeResolution() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, List.of(centaur.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(centaur));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Centaur Courser")).isZero();
        assertThat(countPermanents(player1, "Young Hero")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(centaur);
    }

    @Test
    void youngHeroDoesNotTriggerWhenToughnessExceedsThree() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(centaur.getId()));
        Permanent returned = findPermanent(player1, "Centaur Courser");
        returned.setSummoningSick(false);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(returned))));

        assertThat(gd.stack).isEmpty();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void youngHeroRechecksToughnessWhenAttackTriggerResolves() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(centaur.getId()));
        Permanent returned = findPermanent(player1, "Centaur Courser");
        returned.setSummoningSick(false);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(returned))));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
