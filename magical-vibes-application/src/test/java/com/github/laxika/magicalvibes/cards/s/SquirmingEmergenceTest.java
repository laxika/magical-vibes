package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquirmingEmergence.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class,
        HolyDay.class, LlanowarElves.class})
class SquirmingEmergenceTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentToTheBattlefield() {
        Card target = new GrizzlyBears();
        Card otherPermanent = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(target, otherPermanent));
        harness.setHand(player1, List.of(new SquirmingEmergence()));
        addEmergenceMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void countsTheTargetPermanentCardItself() {
        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SquirmingEmergence()));
        addEmergenceMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    void doesNotCountNonPermanentCardsForTheManaValueLimit() {
        Card target = new GrizzlyBears();
        Card nonPermanent = new HolyDay();
        harness.setGraveyard(player1, List.of(target, nonPermanent));
        harness.setHand(player1, List.of(new SquirmingEmergence()));
        addEmergenceMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetALandCard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SquirmingEmergence()));
        addEmergenceMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addEmergenceMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
