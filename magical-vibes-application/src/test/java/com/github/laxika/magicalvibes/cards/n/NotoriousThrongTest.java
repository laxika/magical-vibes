package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotoriousThrongTest extends BaseCardTest {

    private List<Permanent> faerieRogueTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Faerie Rogue"))
                .toList();
    }

    @Test
    @DisplayName("Normal cast creates one flying Faerie Rogue token per damage dealt to opponents this turn")
    void normalCastCreatesTokensPerDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 3);

        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 4); // normal {3}{U}
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = faerieRogueTokens();
        assertThat(tokens).hasSize(3);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.FAERIE, CardSubtype.ROGUE);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        }
        // Normal cast — no prowl, so no extra turn.
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("No damage to opponents creates no tokens")
    void noDamageCreatesNoTokens() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).isEmpty();
    }

    @Test
    @DisplayName("Prowl cast creates tokens and queues an extra turn for the caster")
    void prowlCastQueuesExtraTurn() {
        setupProwl();
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 2);

        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6); // prowl {5}{U}
        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(2);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Prowl cost is unavailable without combat damage from a Rogue this turn")
    void prowlUnavailableWithoutRogueDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupProwl() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ROGUE);
    }
}
