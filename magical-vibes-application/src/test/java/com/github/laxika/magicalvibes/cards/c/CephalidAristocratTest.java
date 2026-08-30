package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidAristocrat.class, Forest.class, ProdigalPyromancer.class, Shock.class})
class CephalidAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two cards when targeted by a spell")
    void millsWhenTargetedBySpell() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, aristocrat.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }

    @Test
    @DisplayName("Mills two cards when targeted by an ability")
    void millsWhenTargetedByAbility() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CephalidAristocrat());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, aristocrat.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Forest");
    }
}
