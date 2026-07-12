package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LesserGargadonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Lesser Gargadon sacrifices its controller's only land")
    void attackingSacrificesLand() {
        addReadyGargadon(player1);
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
    }

    @Test
    @DisplayName("Blocking with Lesser Gargadon sacrifices its controller's only land")
    void blockingSacrificesLand() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadyGargadon(player2);
        harness.addToBattlefield(player2, new Mountain());

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mountain"));
    }

    @Test
    @DisplayName("With multiple lands, controller chooses which one to sacrifice")
    void multipleLandsPromptChoice() {
        addReadyGargadon(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        long lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain"))
                .count();
        assertThat(lands).isEqualTo(1);
    }

    @Test
    @DisplayName("With no lands, the attack trigger does nothing")
    void noLandsIsHarmless() {
        addReadyGargadon(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lesser Gargadon");
    }

    // ===== Helpers =====

    private Permanent addReadyGargadon(Player player) {
        Permanent perm = new Permanent(new LesserGargadon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
