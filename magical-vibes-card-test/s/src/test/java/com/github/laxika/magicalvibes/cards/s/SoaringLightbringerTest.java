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
        resolveAllTriggers();

        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.isTapped()).isTrue();
        assertThat(glimmer.isAttackedThisTurn()).isFalse();
        assertThat(glimmer.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, glimmer, Keyword.FLYING)).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    void multipleAttackersCreateOnlyOneGlimmerForTheAttackedPlayer() {
        addCreatureReady(player1, new SoaringLightbringer());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Glimmer")).hasSize(1);
        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.isTapped()).isTrue();
        assertThat(glimmer.isAttacking()).isTrue();
        assertThat(glimmer.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(findPermanents(player2, "Glimmer")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantFlyingToOpposingEnchantmentCreatures() {
        addCreatureReady(player1, new SoaringLightbringer());
        Permanent opposingCreature = addCreatureReady(player2, new NyxbornBehemoth());

        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void opponentAttackingDoesNotCreateAGlimmer() {
        addCreatureReady(player1, new SoaringLightbringer());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Glimmer")).isEmpty();
        assertThat(findPermanents(player2, "Glimmer")).isEmpty();
    }
}
