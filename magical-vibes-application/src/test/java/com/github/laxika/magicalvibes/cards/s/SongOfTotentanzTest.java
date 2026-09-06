package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.github.laxika.magicalvibes.model.Keyword.HASTE;
import static com.github.laxika.magicalvibes.model.ManaColor.COLORLESS;
import static com.github.laxika.magicalvibes.model.ManaColor.RED;

@CardUsed({SongOfTotentanz.class, GrizzlyBears.class})
class SongOfTotentanzTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X hasty Rat tokens and gives haste to creatures you control")
    void createsHastyRatsAndGivesHasteToExistingCreatures() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        List<Permanent> rats = ratsOf(player1);
        assertThat(rats).hasSize(2);
        assertThat(rats).allMatch(rat -> gqs.hasKeyword(gd, rat, HASTE));
        assertThat(gqs.hasKeyword(gd, existingCreature, HASTE)).isTrue();
    }

    @Test
    @DisplayName("Rat tokens cannot block")
    void ratsCannotBlock() {
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With X=0, creates no Rat tokens")
    void xZeroCreatesNoRats() {
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ratsOf(player1)).isEmpty();
    }

    private List<Permanent> ratsOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Rat".equals(permanent.getCard().getName()))
                .toList();
    }
}
