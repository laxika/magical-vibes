package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianSwarmlordTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has UPKEEP_TRIGGERED CreateTokenPerOpponentPoisonCounterEffect")
    void hasCorrectEffect() {
        PhyrexianSwarmlord card = new PhyrexianSwarmlord();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenPerOpponentPoisonCounterEffect.class);
    }

    // ===== No poison counters =====

    @Test
    @DisplayName("No tokens created when opponent has no poison counters")
    void noTokensWhenNoPoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Opponent has poison counters =====

    @Test
    @DisplayName("Creates tokens equal to opponent's poison counters")
    void createsTokensEqualToOpponentPoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(3);

        Permanent insectToken = tokens.getFirst();
        assertThat(insectToken.getCard().getName()).isEqualTo("Phyrexian Insect");
        assertThat(insectToken.getCard().getPower()).isEqualTo(1);
        assertThat(insectToken.getCard().getToughness()).isEqualTo(1);
        assertThat(insectToken.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(insectToken.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN, CardSubtype.INSECT);
        assertThat(insectToken.getCard().getKeywords()).contains(Keyword.INFECT);
        assertThat(insectToken.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    // ===== Controller's own poison counters don't count =====

    @Test
    @DisplayName("Controller's own poison counters do not create tokens")
    void controllerPoisonCountersDoNotCount() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player1.getId(), 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        advanceToUpkeep(player2); // opponent's upkeep

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Tokens accumulate over multiple upkeeps =====

    @Test
    @DisplayName("Creates tokens on each upkeep, accumulating over multiple turns")
    void tokensAccumulateOverMultipleUpkeeps() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        // First upkeep - 2 tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Second upkeep - 2 more tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(4);
    }

    // ===== Token count scales with increasing poison =====

    @Test
    @DisplayName("Token count increases as opponent gains more poison counters")
    void tokenCountScalesWithPoisonIncrease() {
        harness.addToBattlefield(player1, new PhyrexianSwarmlord());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        // First upkeep - 1 token
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);

        // Opponent gains more poison
        gd.playerPoisonCounters.put(player2.getId(), 4);

        // Second upkeep - 4 more tokens
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(5); // 1 + 4
    }
}
