package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MesmerizingBenthid.class, SauroformHybrid.class})
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

        blockWithTokens(attacker, List.of(token));
        harness.passBothPriorities();

        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Hexproof persists with one Illusion but opposing Illusions do not count")
    void onlyControlledIllusionsEnableHexproof() {
        Permanent benthid = castBenthid();
        List<Permanent> tokens = illusionTokens(player1);
        gd.playerBattlefields.get(player1.getId()).remove(tokens.getFirst());
        gd.playerBattlefields.get(player2.getId()).add(tokens.getFirst());

        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(tokens.getLast());
        gd.playerBattlefields.get(player2.getId()).add(tokens.getLast());

        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(tokens.getFirst());
        gd.playerBattlefields.get(player1.getId()).add(tokens.getFirst());

        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The enter trigger creates tokens even after Benthid leaves")
    void enterTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of(new MesmerizingBenthid()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent benthid = findPermanent(player1, "Mesmerizing Benthid");

        assertThat(illusionTokens(player1)).isEmpty();
        assertThat(gqs.hasKeyword(gd, benthid, Keyword.HEXPROOF)).isFalse();
        gd.playerBattlefields.get(player1.getId()).remove(benthid);
        resolveAllTriggers();

        assertThat(illusionTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Tokens retain their block ability after Benthid leaves and the trigger survives token removal")
    void tokenAbilitySurvivesBenthidAndTokenLeaving() {
        Permanent benthid = castBenthid();
        Permanent token = illusionTokens(player1).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(benthid);
        Permanent attacker = addAttackingCreature(player2);
        attacker.setTapped(true);

        blockWithTokens(attacker, List.of(token));
        assertThat(attacker.getSkipUntapCount()).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        resolveAllTriggers();

        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two Illusions blocking a hexproof creature prevent only its next untap")
    void multipleNonTargetingTriggersSkipOnlyOneUntap() {
        castBenthid();
        Permanent attacker = harness.enterBattlefieldAndReturn(player2, new MesmerizingBenthid());
        resolveAllTriggers();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setTapped(true);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HEXPROOF)).isTrue();

        blockWithTokens(attacker, illusionTokens(player1));
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(attacker.isTapped()).isFalse();
    }

    private void blockWithTokens(Permanent attacker, List<Permanent> tokens) {
        prepareDeclareBlockers(player2);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = tokens.stream()
                .map(token -> new BlockerAssignment(
                        gd.playerBattlefields.get(player1.getId()).indexOf(token), attackerIndex))
                .toList();
        gs.declareBlockers(gd, player1, assignments);
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
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new SauroformHybrid());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }
}
