package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RepairAndRecharge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkitterbeamBattalion.class, RepairAndRecharge.class, SoulPartition.class})
class SkitterbeamBattalionTest extends BaseCardTest {

    @Test
    void castCreatesTwoTokenCopies() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(3);
        assertThat(findPermanents(player1, "Skitterbeam Battalion").stream().filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2);
    }

    @Test
    void prototypeCastCreatesTwoPrototypeTokenCopies() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(3);
        assertThat(findPermanents(player1, "Skitterbeam Battalion")).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
            assertThat(gqs.getEffectiveColors(gd, permanent)).containsExactly(CardColor.RED);
        });
    }

    @Test
    void enteringWithoutBeingCastDoesNotCreateTokenCopies() {
        harness.enterBattlefieldAndReturn(player1, new SkitterbeamBattalion());

        assertThat(gd.stack).isEmpty();

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(1);
    }

    @Test
    void reanimationDoesNotCreateCopies() {
        SkitterbeamBattalion battalion = new SkitterbeamBattalion();
        harness.setGraveyard(player1, List.of(battalion));
        harness.setHand(player1, List.of(new RepairAndRecharge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, battalion.getId());

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesDoNotCopyCountersOrTriggerRecursively() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent original = findPermanents(player1, "Skitterbeam Battalion").getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(findPermanents(player1, "Skitterbeam Battalion").stream().filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2).allSatisfy(token -> {
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
                });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prototypeCopiesUseLastKnownCharacteristicsAfterSourceIsExiled() {
        harness.setHand(player1, List.of(new SkitterbeamBattalion(), new SoulPartition()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent original = findPermanents(player1, "Skitterbeam Battalion").getFirst();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, original.getId());
        assertThat(findPermanents(player1, "Skitterbeam Battalion")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Skitterbeam Battalion")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.RED);
        });
        assertThat(gd.stack).isEmpty();
    }
}
