package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarSqueak.class, GrizzlyBears.class})
class WarSqueakTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsBoostAndHaste() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new WarSqueak());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void entersAndStopsAnOpponentCreatureFromBlocking() {
        Permanent enchanted = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchanted);
        Permanent blocker = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setHand(player1, List.of(new WarSqueak()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, List.of(enchanted.getId(), blocker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        assertThat(enchanted.isCantBlockThisTurn()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.HASTE)).isTrue();
    }

    @Test
    void mayEnchantTheSameOpponentCreatureItTargetsForTheEtbAbility() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.setHand(player1, List.of(new WarSqueak()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }
}
