package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheReaverCleaver.class, GrizzlyBears.class})
class TheReaverCleaverTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void createsTreasureTokensEqualToCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cleaver = addCleaverReady(player1);
        cleaver.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(3);
    }

    @Test
    void canEquipForThreeMana() {
        Permanent cleaver = addCleaverReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addCleaverReady(Player player) {
        Permanent permanent = new Permanent(new TheReaverCleaver());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
