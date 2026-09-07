package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnTriumphant.class, CentaurCourser.class, SerraAngel.class})
class ReturnTriumphantTest extends BaseCardTest {

    @Test
    void returnsCreatureWithManaValueThreeOrLessAndAttachesYoungHeroRole() {
        CentaurCourser centaur = new CentaurCourser();
        harness.setGraveyard(player1, List.of(centaur));
        UUID centaurId = centaur.getId();
        harness.setHand(player1, List.of(new ReturnTriumphant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, centaurId);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, centaur.getId());
        harness.passBothPriorities();
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

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
