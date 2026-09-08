package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TooEvilToStayDead.class, CrawWurm.class, GrizzlyBears.class})
class TooEvilToStayDeadTest extends BaseCardTest {

    @Test
    void returnsCreatureWithManaValueFourOrLessWithoutTeamwork() {
        Card target = new GrizzlyBears();

        cast(target, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
    }

    @Test
    void cannotTargetCreatureWithManaValueAboveFourWithoutTeamwork() {
        Card target = new CrawWurm();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 4 or less");
    }

    @Test
    void returnsCreatureWithAnyManaValueWhenTeamworkIsPaid() {
        Card target = new CrawWurm();
        Permanent firstTeammate = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTeammate = addCreatureReady(player1, new GrizzlyBears());

        cast(target, List.of(firstTeammate.getId(), secondTeammate.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == target);
        assertThat(firstTeammate.isTapped()).isTrue();
        assertThat(secondTeammate.isTapped()).isTrue();
    }

    private void cast(Card target, List<java.util.UUID> teamworkPermanents) {
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new TooEvilToStayDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), teamworkPermanents);
        harness.passBothPriorities();
    }
}
