package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyclopeanSnare.class, Forest.class, GrizzlyBears.class})
class CyclopeanSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and returns itself to its owner's hand")
    void tapsCreatureAndReturnsToHand() {
        Permanent snare = addSnareReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(snare.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Cyclopean Snare"));

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Cyclopean Snare");
        harness.assertInHand(player1, "Cyclopean Snare");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent snare = addSnareReady(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(snare.isTapped()).isFalse();
    }

    private Permanent addSnareReady(Player player) {
        Permanent permanent = new Permanent(new CyclopeanSnare());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
