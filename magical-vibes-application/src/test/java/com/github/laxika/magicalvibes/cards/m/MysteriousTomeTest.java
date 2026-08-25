package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysteriousTome.class, GrizzlyBears.class, Island.class})
class MysteriousTomeTest extends BaseCardTest {

    @Test
    void drawsAndTransforms() {
        Permanent tome = addTomeReady(player1);
        gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, tome), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(tome.isTransformed()).isTrue();
    }

    @Test
    void backFaceTapsTargetAndTransformsBack() {
        Permanent tome = addTransformedTome(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, tome), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(tome.isTransformed()).isFalse();
    }

    @Test
    void backFaceCannotTargetLand() {
        Permanent tome = addTransformedTome(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, tome), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTomeReady(Player player) {
        MysteriousTome card = new MysteriousTome();
        Permanent tome = new Permanent(card);
        tome.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tome);
        return tome;
    }

    private Permanent addTransformedTome(Player player) {
        MysteriousTome card = new MysteriousTome();
        Permanent tome = new Permanent(card);
        tome.setSummoningSick(false);
        tome.setCard(card.getBackFaceCard());
        tome.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(tome);
        return tome;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
