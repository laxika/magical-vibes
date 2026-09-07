package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkProphecy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OldHobAlleycatBlues.class, DarkProphecy.class})
class OldHobAlleycatBluesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty 2/2 red Mutant token at the beginning of your combat")
    void createsMutantAtBeginningOfCombat() {
        addOldHobReady(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Mutant").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Destroys the created token at the beginning of the next end step")
    void destroysCreatedTokenAtNextEndStep() {
        harness.addToBattlefield(player1, new DarkProphecy());
        addOldHobReady(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Mutant")).hasSize(1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutant")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not create a token during an opponent's combat")
    void doesNotCreateTokenOnOpponentsTurn() {
        addOldHobReady(player1);

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutant")).isEmpty();
    }

    @Test
    @DisplayName("Grants indestructible to an attacking creature token until end of turn")
    void grantsIndestructibleToAttackingToken() {
        Permanent oldHob = addOldHobReady(player1);
        Permanent token = addAttackingToken(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, oldHob), null, token.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-token creature")
    void cannotTargetNonTokenCreature() {
        Permanent oldHob = addOldHobReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, oldHob), null, oldHob.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature token");
    }

    private Permanent addOldHobReady(Player player) {
        return addCreatureReady(player, new OldHobAlleycatBlues());
    }

    private Permanent addAttackingToken(Player player) {
        Card tokenCard = new Card();
        tokenCard.setName("Test Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);

        Permanent token = new Permanent(tokenCard);
        token.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(token);
        return token;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(activePlayer);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
