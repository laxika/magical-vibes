package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CecilDarkKnight.class, CecilRedeemedPaladin.class, GrizzlyBears.class, HillGiant.class})
class CecilDarkKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Cecil loses the damage dealt and transforms after reaching half life")
    void darknessTransformsAtHalfLife() {
        Permanent cecil = addReadyCecil(player1);
        harness.setLife(player1, 12);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(cecil.isTransformed()).isTrue();
        assertThat(cecil.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cecil does not transform when the life-loss trigger leaves its controller above half life")
    void darknessDoesNotTransformAboveHalfLife() {
        Permanent cecil = addReadyCecil(player1);
        harness.setLife(player1, 13);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(cecil.isTransformed()).isFalse();
        assertThat(cecil.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cecil still causes life loss when it dies while dealing damage")
    void darknessDoesNotTransformAfterCecilDies() {
        Permanent cecil = addReadyCecil(player1);
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        harness.setLife(player1, 12);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(cecil))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cecil);
    }

    @Test
    @DisplayName("Cecil protects other attacking creatures but not itself")
    void protectGrantsIndestructibleToOtherAttackers() {
        Permanent cecil = addTransformedCecil(player1);
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cecil, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReadyCecil(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyPermanent(player, new CecilDarkKnight());
    }

    private Permanent addTransformedCecil(com.github.laxika.magicalvibes.model.Player player) {
        CecilDarkKnight front = new CecilDarkKnight();
        Permanent cecil = new Permanent(front);
        cecil.setCard(front.getBackFaceCard());
        cecil.setTransformed(true);
        cecil.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(cecil);
        return cecil;
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
