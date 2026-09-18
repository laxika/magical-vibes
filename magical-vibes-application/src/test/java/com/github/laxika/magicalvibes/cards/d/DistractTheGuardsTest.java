package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DistractTheGuards.class, GrizzlyBears.class})
class DistractTheGuardsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three white Human Rogue tokens")
    void createsHumanRogueTokens() {
        harness.setHand(player1, List.of(new DistractTheGuards()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ROGUE);
        });
    }

    @Test
    @DisplayName("Freerunning is available after Assassin combat damage")
    void castsForFreerunningAfterAssassinDamage() {
        markAssassinCombatDamage();
        castForFreerunning();

        assertThat(countTokens()).isEqualTo(3);
    }

    @Test
    @DisplayName("Freerunning is available after commander combat damage")
    void castsForFreerunningAfterCommanderDamage() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.combatDamageToPlayersThisTurn.put(commander.getId(), ConcurrentHashMap.newKeySet());
        gd.combatDamageToPlayersThisTurn.get(commander.getId()).add(player2.getId());
        gd.damageSourcesControlledByPlayerThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(commander.getId());
        gd.combatDamageSourcesThatWereCommandersThisTurn.add(commander.getId());

        castForFreerunning();

        assertThat(countTokens()).isEqualTo(3);
    }

    @Test
    @DisplayName("Freerunning is unavailable without qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        harness.setHand(player1, List.of(new DistractTheGuards()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }

    private void castForFreerunning() {
        harness.setHand(player1, List.of(new DistractTheGuards()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private long countTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
