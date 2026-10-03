package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.DeckFormat;
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

        declareAttackersAndPrepareBlockers(player1, List.of(0));
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

    @Test
    @DisplayName("Darkness uses half the Commander starting life total")
    void darknessTransformsAtCommanderHalfLife() {
        gd.format = DeckFormat.COMMANDER;
        Permanent cecil = addReadyCecil(player1);
        harness.setLife(player1, 22);
        harness.setLife(player2, 40);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 38);
        assertThat(cecil.isTransformed()).isTrue();
        assertThat(cecil.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Darkness transforms when life falls below half")
    void darknessTransformsBelowHalfLife() {
        Permanent cecil = addReadyCecil(player1);
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 8);
        assertThat(cecil.isTransformed()).isTrue();
        assertThat(cecil.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The redeemed face gains life from damage without triggering Darkness")
    void redeemedPaladinGainsLifeWithoutDarkness() {
        Permanent cecil = addTransformedCecil(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
        assertThat(cecil.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Protect saves another attacker from lethal combat damage")
    void protectPreventsCombatDestruction() {
        addTransformedCecil(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        resolveAllTriggers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    private Permanent addReadyCecil(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CecilDarkKnight());
    }

    private Permanent addTransformedCecil(com.github.laxika.magicalvibes.model.Player player) {
        CecilDarkKnight front = new CecilDarkKnight();
        Permanent cecil = addCreatureReady(player, front);
        cecil.setCard(front.getBackFaceCard());
        cecil.setTransformed(true);
        return cecil;
    }
}
