package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NotoriousThrong.class, PricklyBoggart.class, MothdustChangeling.class})
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

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
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
    @DisplayName("Created tokens are black creatures")
    void createdTokensAreBlackCreatureTokens() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
        harness.passBothPriorities();

        Permanent token = faerieRogueTokens().getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Damage dealt to the caster does not count toward token creation")
    void damageToCasterDoesNotCount() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.damageDealtToPlayersThisTurn.put(player1.getId(), 5);
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 2);

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(2);
    }

    @Test
    @DisplayName("No damage to opponents creates no tokens")
    void noDamageCreatesNoTokens() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
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

    @Test
    @DisplayName("Normal cost remains available after Rogue combat damage and gives no extra turn")
    void normalCostAfterRogueDamageDoesNotGrantExtraTurn() {
        setupProwl();
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Token count is determined on resolution rather than when the spell is cast")
    void tokenCountUsesDamageAtResolution() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player1, new NotoriousThrong(), "{3}{U}");
        gd.damageDealtToPlayersThisTurn.merge(player2.getId(), 2, Integer::sum);
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(3);
    }

    @Test
    @DisplayName("Opponent's Rogue combat damage does not enable the caster's prowl")
    void opponentRogueDamageDoesNotEnableProwl() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player2.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ROGUE);
        gd.damageDealtToPlayersThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage to an opponent alone does not enable prowl without qualifying combat damage")
    void noncombatDamageDoesNotEnableProwl() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prowl requires six mana even when the four-mana normal cost could be paid")
    void prowlRequiresFullAlternativeCost() {
        setupProwl();
        gd.damageDealtToPlayersThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(faerieRogueTokens()).isEmpty();
    }

    @Test
    @DisplayName("Rogue combat damage enables prowl even after that Rogue leaves the battlefield")
    void rogueDamageEnablesProwlAfterSourceLeaves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new PricklyBoggart());
        rogue.setSummoningSick(false);
        rogue.setAttacking(true);
        rogue.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        harness.assertLife(player2, 19);
        gd.playerBattlefields.get(player1.getId()).remove(rogue);
        harness.setGraveyard(player1, List.of(rogue.getCard()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Changeling combat damage qualifies as Rogue damage for prowl")
    void changelingCombatDamageEnablesProwl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new MothdustChangeling());
        changeling.setSummoningSick(false);
        changeling.setAttacking(true);
        changeling.setAttackTarget(player2.getId());
        harness.resolveCombatDamage();
        harness.assertLife(player2, 19);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new NotoriousThrong()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castWithProwl(player1, 0, null);
        harness.passBothPriorities();

        assertThat(faerieRogueTokens()).hasSize(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }
}
