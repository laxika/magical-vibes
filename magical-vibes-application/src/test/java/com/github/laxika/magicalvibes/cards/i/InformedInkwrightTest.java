package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InformedInkwrightTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting an instant that targets a creature creates a 1/1 flying Inkling token")
    void reparteeCreatesToken() {
        harness.addToBattlefield(player1, new InformedInkwright());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent inkling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Inkling"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new InformedInkwright());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Inkling"));
    }
}
