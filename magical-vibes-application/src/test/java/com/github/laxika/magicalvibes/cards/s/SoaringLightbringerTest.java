package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornBehemoth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoaringLightbringer.class, NyxbornBehemoth.class, GrizzlyBears.class})
class SoaringLightbringerTest extends BaseCardTest {

    @Test
    void grantsFlyingOnlyToOtherEnchantmentCreaturesYouControl() {
        addCreatureReady(player1, new SoaringLightbringer());
        Permanent enchantmentCreature = addCreatureReady(player1, new NyxbornBehemoth());
        Permanent regularCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, enchantmentCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, regularCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void attackingPlayerCreatesTappedAttackingGlimmer() {
        addCreatureReady(player1, new SoaringLightbringer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.isTapped()).isTrue();
        assertThat(glimmer.isAttackedThisTurn()).isTrue();
        assertThat(glimmer.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, glimmer, Keyword.FLYING)).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
    }
}
