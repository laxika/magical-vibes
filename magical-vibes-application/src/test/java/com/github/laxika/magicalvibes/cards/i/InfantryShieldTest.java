package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfantryShield.class, GrizzlyBears.class})
class InfantryShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has menace and mobilizes Warriors equal to its power")
    void equippedCreatureGainsMenaceAndMobilize() {
        Permanent creature = addCreatureReady(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Mobilized Warrior tokens are sacrificed at the next end step")
    void mobilizedTokensAreSacrificedAtNextEndStep() {
        Permanent creature = addCreatureReady(player1);
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(warriorTokens(player1)).hasSize(2);

        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).isEmpty();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addShieldReady(Player player) {
        Permanent permanent = new Permanent(new InfantryShield());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Permanent> warriorTokens(Player player) {
        return findPermanents(player, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
