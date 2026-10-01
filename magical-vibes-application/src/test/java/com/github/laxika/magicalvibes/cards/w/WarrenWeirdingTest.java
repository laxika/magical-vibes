package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarrenWeirding.class, PricklyBoggart.class, ElvishWarrior.class})
class WarrenWeirdingTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Target player sacrifices their only creature (a Goblin) and creates two hasty Goblin Rogue tokens")
    void goblinSacrificedCreatesTokens() {
        addCreatureReady(player2, new PricklyBoggart());

        castAtPlayer2();

        // The Goblin was sacrificed.
        harness.assertNotOnBattlefield(player2, "Prickly Boggart");
        harness.assertInGraveyard(player2, "Prickly Boggart");

        // The sacrificing player created two Goblin Rogue tokens...
        List<Permanent> tokens = findPermanents(player2, "Goblin Rogue");
        assertThat(tokens).hasSize(2);

        // ...with haste, under the target player's control (not the caster's).
        for (Permanent token : tokens) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ROGUE);
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        }
        assertThat(countPermanents(player1, "Goblin Rogue")).isZero();
    }

    @Test
    @DisplayName("Non-Goblin sacrifice does not create tokens")
    void nonGoblinSacrificeCreatesNoTokens() {
        addCreatureReady(player2, new ElvishWarrior());

        castAtPlayer2();

        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Elvish Warrior");
        assertThat(countPermanents(player2, "Goblin Rogue")).isZero();
    }

    @Test
    @DisplayName("With multiple creatures, target player chooses which to sacrifice; choosing the Goblin creates tokens")
    void choosingGoblinAmongCreaturesCreatesTokens() {
        Permanent goblin = addCreatureReady(player2, new PricklyBoggart());
        addCreatureReady(player2, new ElvishWarrior());

        castAtPlayer2();

        // Resolution pauses for the target player's sacrifice choice.
        harness.handlePermanentChosen(player2, goblin.getId());

        harness.assertNotOnBattlefield(player2, "Prickly Boggart");
        harness.assertOnBattlefield(player2, "Elvish Warrior");
        assertThat(countPermanents(player2, "Goblin Rogue")).isEqualTo(2);
    }

    @Test
    @DisplayName("With multiple creatures, choosing a non-Goblin creates no tokens")
    void choosingNonGoblinCreatesNoTokens() {
        addCreatureReady(player2, new PricklyBoggart());
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());

        castAtPlayer2();

        harness.handlePermanentChosen(player2, warrior.getId());

        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Prickly Boggart");
        assertThat(countPermanents(player2, "Goblin Rogue")).isZero();
    }

    @Test
    @DisplayName("Target player with no creatures sacrifices nothing and creates no tokens")
    void noCreaturesNoEffect() {
        castAtPlayer2();

        assertThat(countPermanents(player2, "Goblin Rogue")).isZero();
    }

    @Test
    @DisplayName("Goblin Rogue tokens lose the granted haste at the end of the turn")
    void tokenHasteExpiresAtEndOfTurn() {
        addCreatureReady(player2, new PricklyBoggart());

        castAtPlayer2();

        List<Permanent> tokens = findPermanents(player2, "Goblin Rogue");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse());
    }
}
