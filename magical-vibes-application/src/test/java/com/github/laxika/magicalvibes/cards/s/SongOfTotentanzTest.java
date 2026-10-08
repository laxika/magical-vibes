package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoryMice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SongOfTotentanz.class, ArmoryMice.class})
class SongOfTotentanzTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X hasty Rat tokens and gives haste to creatures you control")
    void createsHastyRatsAndGivesHasteToExistingCreatures() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);

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

        harness.castAndResolveSorcery(player1, 0, 1);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
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

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ratsOf(player1)).isEmpty();
    }

    @Test
    @DisplayName("X=0 still gives haste to existing creatures")
    void xZeroStillGrantsHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ratsOf(player1)).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste affects only your creatures present when the spell resolves")
    void doesNotGrantHasteToOpponentsOrLaterCreatures() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());

        assertThat(ratsOf(player1)).hasSize(1);
        assertThat(ratsOf(player1)).allMatch(rat -> gqs.hasKeyword(gd, rat, HASTE));
        assertThat(ratsOf(player2)).isEmpty();
        assertThat(gqs.hasKeyword(gd, opponentCreature, HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste expires at end of turn but the Rats remain")
    void hasteExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(gqs.hasKeyword(gd, creature, HASTE)).isTrue();
        assertThat(ratsOf(player1)).allMatch(rat -> gqs.hasKeyword(gd, rat, HASTE));

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(ratsOf(player1)).hasSize(1);
        assertThat(ratsOf(player1)).allMatch(rat -> !gqs.hasKeyword(gd, rat, HASTE));
        assertThat(gqs.hasKeyword(gd, creature, HASTE)).isFalse();
    }

    @Test
    @DisplayName("Created tokens are black 1/1 Rat creatures")
    void createsCorrectRatTokens() {
        harness.setHand(player1, List.of(new SongOfTotentanz()));
        harness.addMana(player1, RED, 1);
        harness.addMana(player1, COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(ratsOf(player1)).hasSize(3).allSatisfy(rat -> {
            assertThat(gqs.isCreature(gd, rat)).isTrue();
            assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, rat)).containsExactly(CardColor.BLACK);
            assertThat(rat.getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        });
    }

    private List<Permanent> ratsOf(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Rat".equals(permanent.getCard().getName()))
                .toList();
    }
}
