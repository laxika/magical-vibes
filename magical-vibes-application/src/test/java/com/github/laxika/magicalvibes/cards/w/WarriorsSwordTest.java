package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WarriorsSword.class, GrizzlyBears.class})
class WarriorsSwordTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new WarriorsSword()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sword = findPermanent(player1, "Warrior's Sword");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(sword.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.WARRIOR);
    }

    @Test
    void equipMovesSwordAndItsBonuses() {
        Permanent sword = addSwordReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        sword.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.WARRIOR);
    }

    private Permanent addSwordReady(Player player) {
        Permanent permanent = new Permanent(new WarriorsSword());
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
