package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaystonesGuidance.class, GrizzlyBears.class})
class WaystonesGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature spell each turn mobilizes 1")
    void firstCreatureSpellMobilizes() {
        harness.addToBattlefield(player1, new WaystonesGuidance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        declareAttackers(List.of(indexOf(player1, bears)));
        resolveAllTriggers();

        List<Permanent> warriors = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(warriors).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, warriors.getFirst())).isEqualTo(2);
    }

    @Test
    @DisplayName("Only attacking tokens get +1/+0")
    void onlyAttackingTokensGetBoost() {
        harness.addToBattlefield(player1, new WaystonesGuidance());
        Permanent nonAttackingToken = addCreatureReady(player1, new GrizzlyBears());
        nonAttackingToken.getCard().setToken(true);
        Permanent attackingToken = addCreatureReady(player1, new GrizzlyBears());
        attackingToken.getCard().setToken(true);
        markAttacking(attackingToken);

        assertThat(gqs.getEffectivePower(gd, nonAttackingToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attackingToken)).isEqualTo(3);
    }

    private void markAttacking(Permanent permanent) {
        permanent.setAttacking(true);
    }
}
