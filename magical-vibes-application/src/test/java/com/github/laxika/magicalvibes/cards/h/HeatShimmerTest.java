package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HeatShimmerTest extends BaseCardTest {

    private void castHeatShimmer(UUID targetId) {
        harness.setHand(player1, List.of(new HeatShimmer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent token(UUID controllerId) {
        return gd.playerBattlefields.get(controllerId).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Creates a token copy of target creature with haste")
    void createsHastyTokenCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castHeatShimmer(harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(token(player1.getId()).getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castHeatShimmer(harness.getPermanentId(player2, "Grizzly Bears"));

        // Token enters under the caster's control.
        assertThat(token(player1.getId())).isNotNull();
    }

    @Test
    @DisplayName("Token can attack the turn it is created thanks to haste")
    void tokenCanAttackDueToHaste() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castHeatShimmer(harness.getPermanentId(player1, "Grizzly Bears"));

        GameService gs = harness.getGameService();
        Permanent tokenPermanent = token(player1.getId());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tokenPermanent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(attackerIndex));

        assertThat(tokenPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castHeatShimmer(harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Original creature remains on the battlefield")
    void originalCreatureRemains() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castHeatShimmer(harness.getPermanentId(player1, "Grizzly Bears"));

        long count = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(count).isEqualTo(2);
    }
}
