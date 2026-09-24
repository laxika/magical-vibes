package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TadeasJuniperAscendant.class, GrizzlyBears.class, HillGiant.class, RagingGoblin.class, Shock.class})
class TadeasJuniperAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof while not attacking and loses it while attacking")
    void hexproofUnlessAttacking() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());

        assertThat(gqs.hasKeyword(gd, tadeas, Keyword.HEXPROOF)).isTrue();

        tadeas.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, tadeas, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Tadeas")
    void hexproofPreventsOpponentTargeting() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, tadeas.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("A reach attacker is untapped and can be blocked only by creatures of no greater power")
    void reachAttackTriggerUntapsAndRestrictsBlockers() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());
        Permanent lowPowerBlocker = addCreatureReady(player2, new RagingGoblin());
        Permanent highPowerBlocker = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(tadeas.isTapped()).isFalse();

        int tadeasIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tadeas);
        int highPowerBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(highPowerBlocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(highPowerBlockerIndex, tadeasIndex))))
                .isInstanceOf(IllegalStateException.class);

        int lowPowerBlockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(lowPowerBlocker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(lowPowerBlockerIndex, tadeasIndex)));
        assertThat(lowPowerBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Draws one card when one or more creatures deal combat damage")
    void drawsOnceForMultipleCombatDamageDealers() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());
        tadeas.setAttacking(true);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
