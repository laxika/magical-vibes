package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendersOfHumanity.class, GrizzlyBears.class})
class DefendersOfHumanityTest extends BaseCardTest {

    @Test
    @DisplayName("Entering with X=2 creates two vigilant Astartes Warrior tokens")
    void enteringCreatesTokens() {
        harness.setHand(player1, List.of(new DefendersOfHumanity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(astartesWarriors(player1)).hasSize(2);
        assertThat(astartesWarriors(player1)).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    @DisplayName("Exiling the enchantment creates X tokens when no creatures are controlled")
    void activatedAbilityCreatesTokensAndExilesSource() {
        Permanent defenders = harness.addToBattlefieldAndReturn(player1, new DefendersOfHumanity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(defenders), 2, null);
        harness.passBothPriorities();

        assertThat(astartesWarriors(player1)).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof DefendersOfHumanity);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() instanceof DefendersOfHumanity);
    }

    @Test
    @DisplayName("The activated ability cannot be used while controlling a creature")
    void activatedAbilityRequiresNoCreatures() {
        Permanent defenders = harness.addToBattlefieldAndReturn(player1, new DefendersOfHumanity());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(defenders), 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no creatures");
    }

    @Test
    @DisplayName("The activated ability cannot be used during an opponent's turn")
    void activatedAbilityRequiresYourTurn() {
        Permanent defenders = harness.addToBattlefieldAndReturn(player1, new DefendersOfHumanity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(defenders), 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    private List<Permanent> astartesWarriors(Player player) {
        return findPermanents(player, "Astartes Warrior");
    }
}
