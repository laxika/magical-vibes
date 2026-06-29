package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BoostSelfPerOtherControlledSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Rat Colony has correct static effect")
    void hasCorrectEffect() {
        RatColony card = new RatColony();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostSelfPerOtherControlledSubtypeEffect.class);
        BoostSelfPerOtherControlledSubtypeEffect effect =
                (BoostSelfPerOtherControlledSubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.RAT);
        assertThat(effect.powerPerPermanent()).isEqualTo(1);
        assertThat(effect.toughnessPerPermanent()).isEqualTo(0);
    }

    @Test
    @DisplayName("Rat Colony is 2/1 with no other Rats")
    void baseStatsWithNoOtherRats() {
        Permanent colony = addRatColony(player1);

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony gets +1/+0 for each other Rat Colony you control")
    void countsOtherRatColonies() {
        Permanent colony = addRatColony(player1);
        harness.addToBattlefield(player1, new RatColony());
        harness.addToBattlefield(player1, new RatColony());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony counts other Rats of different names")
    void countsOtherRatsOfDifferentNames() {
        Permanent colony = addRatColony(player1);
        harness.addToBattlefield(player1, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony does not count opponent's Rats")
    void doesNotCountOpponentRats() {
        Permanent colony = addRatColony(player1);
        harness.addToBattlefield(player2, new RatColony());
        harness.addToBattlefield(player2, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony does not count non-Rat creatures")
    void doesNotCountNonRats() {
        Permanent colony = addRatColony(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rat Colony bonus updates when other Rats leave the battlefield")
    void bonusUpdatesWhenRatsLeave() {
        Permanent colony = addRatColony(player1);
        harness.addToBattlefield(player1, new RatColony());
        harness.addToBattlefield(player1, new TyphoidRats());

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Typhoid Rats"));

        assertThat(gqs.getEffectivePower(gd, colony)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, colony)).isEqualTo(1);
    }

    private Permanent addRatColony(Player player) {
        Permanent permanent = new Permanent(new RatColony());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
