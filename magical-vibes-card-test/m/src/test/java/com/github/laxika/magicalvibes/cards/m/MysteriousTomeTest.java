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

    @Test
    void backFaceCanTargetItsOwnTappedArtifact() {
        Permanent tome = addTransformedTome(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, tome), null, tome.getId());
        harness.passBothPriorities();

        assertThat(tome.isTapped()).isTrue();
        assertThat(tome.isTransformed()).isFalse();
    }

    @Test
    void backFaceTransformsWhenTargetIsAlreadyTapped() {
        Permanent tome = addTransformedTome(player1);
        Permanent target = addTomeReady(player2);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, tome), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(tome.isTransformed()).isFalse();
    }

    @Test
    void backFaceDoesNotTransformWhenTargetLeavesBattlefield() {
        Permanent tome = addTransformedTome(player1);
        Permanent target = addTomeReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, tome), null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(tome.isTapped()).isTrue();
        assertThat(tome.isTransformed()).isTrue();
    }

    @Test
    void frontFaceDrawsEvenWhenSourceLeavesBattlefield() {
        Permanent tome = addTomeReady(player1);
        gd.playerDecks.get(player1.getId()).add(0, new Island());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, tome), null, null);
        gd.playerBattlefields.get(player1.getId()).remove(tome);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void transformsBackAndCanUseFrontFaceAgain() {
        Permanent tome = addTomeReady(player1);
        Permanent target = addTomeReady(player2);
        gd.playerDecks.get(player1.getId()).add(0, new Island());
        gd.playerDecks.get(player1.getId()).add(0, new Island());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(player1, tome), null, null);
        harness.passBothPriorities();
        assertThat(tome.isTransformed()).isTrue();
        assertThat(tome.isTapped()).isTrue();

        tome.untap();
        harness.activateAbility(player1, indexOf(player1, tome), null, target.getId());
        harness.passBothPriorities();
        assertThat(tome.isTransformed()).isFalse();
        assertThat(tome.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();

        tome.untap();
        harness.activateAbility(player1, indexOf(player1, tome), null, null);
        harness.passBothPriorities();

        assertThat(tome.isTransformed()).isTrue();
        assertThat(tome.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private Permanent addTomeReady(Player player) {
        return addCreatureReady(player, new MysteriousTome());
    }

    private Permanent addTransformedTome(Player player) {
        Permanent tome = addTomeReady(player);
        tome.setCard(tome.getOriginalCard().getBackFaceCard());
        tome.setTransformed(true);
        return tome;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
