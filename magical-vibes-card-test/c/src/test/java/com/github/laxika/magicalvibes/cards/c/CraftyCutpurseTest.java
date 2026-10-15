package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninWarleader;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CraftyCutpurse.class, AnointedProcession.class, GatherTheTownsfolk.class,
        GrizzlyBears.class, LeoninWarleader.class, SailorOfMeans.class})
class CraftyCutpurseTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent-created creature tokens enter under Crafty Cutpurse's controller")
    void redirectsOpponentTokens() {
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(tokenCount(player1, "Human")).isEqualTo(2);
        assertThat(tokenCount(player2, "Human")).isZero();
    }

    @Test
    @DisplayName("Crafty Cutpurse is applied before an opponent's token multiplier")
    void redirectsBeforeOpponentTokenMultiplier() {
        harness.addToBattlefield(player2, new AnointedProcession());
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(tokenCount(player1, "Human")).isEqualTo(2);
        assertThat(tokenCount(player2, "Human")).isZero();
    }

    @Test
    @DisplayName("Crafty Cutpurse does not change an opponent's nontoken creature")
    void doesNotRedirectOpponentCreature() {
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> "Grizzly Bears".equals(permanent.getCard().getName()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> "Grizzly Bears".equals(permanent.getCard().getName()));
    }

    @Test
    @DisplayName("Crafty Cutpurse's replacement expires at turn cleanup")
    void expiresAtTurnCleanup() {
        resolveCraftyCutpurse();
        advanceToNextTurn(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(tokenCount(player1, "Human")).isZero();
        assertThat(tokenCount(player2, "Human")).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirected tokens use the receiving controller's token multiplier")
    void appliesReceivingControllersMultiplier() {
        harness.addToBattlefield(player1, new AnointedProcession());
        resolveCraftyCutpurse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(tokenCount(player1, "Human")).isEqualTo(4);
        assertThat(tokenCount(player2, "Human")).isZero();
    }

    @Test
    @DisplayName("Treasure tokens are redirected without changing previously created tokens")
    void redirectsNewArtifactTokensOnly() {
        castSailorOfMeans(player2);
        resolveCraftyCutpurse();
        castSailorOfMeans(player2);

        assertThat(tokenCount(player1, "Treasure")).isEqualTo(1);
        assertThat(tokenCount(player2, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's own token creation is unaffected")
    void leavesOwnTokensUnderOwnControl() {
        resolveCraftyCutpurse();
        castSailorOfMeans(player1);

        assertThat(tokenCount(player1, "Treasure")).isEqualTo(1);
        assertThat(tokenCount(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Stolen tapped-and-attacking tokens enter tapped but do not attack")
    void redirectedTokensDoNotAttackForDefendingPlayer() {
        resolveCraftyCutpurse();
        harness.addToBattlefield(player2, new LeoninWarleader());
        Permanent warleader = gd.playerBattlefields.get(player2.getId()).getFirst();
        warleader.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));
        harness.passBothPriorities();

        List<Permanent> cats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Cat".equals(p.getCard().getName()))
                .toList();
        assertThat(cats).hasSize(2);
        assertThat(tokenCount(player2, "Cat")).isZero();
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.isTapped()).isTrue();
            assertThat(cat.isAttacking()).isFalse();
        });
    }

    private void castSailorOfMeans(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new SailorOfMeans()));
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each resolved Cutpurse trigger is a separate replacement effect")
    void multipleTriggersFromOnePlayerRemainDistinct() {
        resolveCraftyCutpurse(player1);
        resolveCraftyCutpurse(player1);
        resolveCraftyCutpurse(player2);

        castSailorOfMeans(player2);

        assertThat(tokenCount(player1, "Treasure")).isEqualTo(1);
        assertThat(tokenCount(player2, "Treasure")).isZero();
    }

    private void resolveCraftyCutpurse() {
        resolveCraftyCutpurse(player1);
    }

    private void resolveCraftyCutpurse(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new CraftyCutpurse()));
        harness.addMana(player, ManaColor.BLUE, 4);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long tokenCount(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
