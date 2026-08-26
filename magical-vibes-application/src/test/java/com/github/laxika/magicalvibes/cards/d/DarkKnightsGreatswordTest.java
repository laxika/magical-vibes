package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkKnightsGreatsword.class, GrizzlyBears.class})
class DarkKnightsGreatswordTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new DarkKnightsGreatsword()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent greatsword = findPermanent(player1, "Dark Knight's Greatsword");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(greatsword.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.KNIGHT);
    }

    @Test
    void equipPaysLifeAndCanOnlyBeActivatedOnceEachTurn() {
        Permanent greatsword = addGreatswordReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        greatsword.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(greatsword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.KNIGHT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, first.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1");
    }

    private Permanent addGreatswordReady(Player player) {
        Permanent permanent = new Permanent(new DarkKnightsGreatsword());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
