package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaystonesGuidance.class, GrizzlyBears.class, Shock.class})
class WaystonesGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature spell each turn mobilizes 1")
    void firstCreatureSpellMobilizes() {
        harness.addToBattlefield(player1, new WaystonesGuidance());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bears)));
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
        var nonAttackingTokenCard = new GrizzlyBears().createRuntimeCopy();
        nonAttackingTokenCard.setToken(true);
        Permanent nonAttackingToken = addCreatureReady(player1, nonAttackingTokenCard);
        var attackingTokenCard = new GrizzlyBears().createRuntimeCopy();
        attackingTokenCard.setToken(true);
        Permanent attackingToken = addCreatureReady(player1, attackingTokenCard);
        markAttacking(attackingToken);

        assertThat(gqs.getEffectivePower(gd, nonAttackingToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attackingToken)).isEqualTo(3);
    }

    private void markAttacking(Permanent permanent) {
        permanent.setAttacking(true);
    }
}
