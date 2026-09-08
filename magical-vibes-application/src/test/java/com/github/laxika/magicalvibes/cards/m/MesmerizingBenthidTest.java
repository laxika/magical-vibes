package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MesmerizingBenthidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two Illusion tokens")
    void entersWithTwoIllusionTokens() {
        castBenthid();

        assertThat(illusionTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Has hexproof while you control an Illusion")
    void hasHexproofWhileControllingIllusion() {
        Permanent benthid = castBenthid();

        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().isToken());

        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("An Illusion token makes a blocked creature not untap next turn")
    void illusionTokenSkipsBlockedCreaturesNextUntap() {
        castBenthid();
        Permanent token = illusionTokens(player1).getFirst();
        Permanent attacker = addAttackingCreature(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    private Permanent castBenthid() {
        harness.setHand(player1, List.of(new MesmerizingBenthid()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Mesmerizing Benthid");
    }

    private List<Permanent> illusionTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }
}
