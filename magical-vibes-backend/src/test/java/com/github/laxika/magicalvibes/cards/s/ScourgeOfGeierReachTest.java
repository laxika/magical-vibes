package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOpponentPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScourgeOfGeierReachTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static +1/+1 per opponent creature effect")
    void hasCorrectEffect() {
        ScourgeOfGeierReach card = new ScourgeOfGeierReach();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerOpponentPermanentEffect.class);
        BoostSelfPerOpponentPermanentEffect effect =
                (BoostSelfPerOpponentPermanentEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentIsCreaturePredicate.class);
        assertThat(effect.powerPerPermanent()).isEqualTo(1);
        assertThat(effect.toughnessPerPermanent()).isEqualTo(1);
    }

    // ===== Base stats without opponent creatures =====

    @Test
    @DisplayName("Without opponent creatures, is 3/3")
    void withoutOpponentCreaturesIs3x3() {
        Permanent scourge = addScourge(player1);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    // ===== With one opponent creature =====

    @Test
    @DisplayName("With one opponent creature, is 4/4")
    void withOneOpponentCreatureIs4x4() {
        Permanent scourge = addScourge(player1);
        addCreature(player2);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(4);
    }

    // ===== With multiple opponent creatures =====

    @Test
    @DisplayName("With three opponent creatures, is 6/6")
    void withThreeOpponentCreaturesIs6x6() {
        Permanent scourge = addScourge(player1);
        addCreature(player2);
        addCreature(player2);
        addCreature(player2);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(6);
    }

    // ===== Own creatures don't count =====

    @Test
    @DisplayName("Own creatures don't affect Scourge's power/toughness")
    void ownCreaturesDontCount() {
        Permanent scourge = addScourge(player1);
        addCreature(player1);
        addCreature(player1);

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    // ===== Opponent non-creature permanents don't count =====

    @Test
    @DisplayName("Opponent's non-creature permanents don't count")
    void opponentNonCreaturesDontCount() {
        Permanent scourge = addScourge(player1);
        gd.playerBattlefields.get(player2.getId()).add(
                new Permanent(new com.github.laxika.magicalvibes.cards.m.Mountain()));

        assertThat(gqs.getEffectivePower(gd, scourge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourge)).isEqualTo(3);
    }

    // ===== Helpers =====

    private Permanent addScourge(Player player) {
        Permanent perm = new Permanent(new ScourgeOfGeierReach());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
