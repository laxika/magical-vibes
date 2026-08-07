package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThopterSpyNetworkTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Permanent> thopterTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Thopter"))
                .toList();
    }

    @Test
    @DisplayName("Upkeep creates a 1/1 colorless Thopter artifact creature token with flying when an artifact is controlled")
    void upkeepCreatesThopterWithArtifact() {
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        harness.addToBattlefield(player1, new Memnite());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = thopterTokens();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire when no artifact is controlled")
    void upkeepDoesNotTriggerWithoutArtifact() {
        // Thopter Spy Network is an enchantment, so it is not an artifact itself.
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(thopterTokens()).isEmpty();
    }

    @Test
    @DisplayName("One artifact creature dealing combat damage draws a card")
    void artifactCreatureDamageDrawsOneCard() {
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        addAttacker(new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Two artifact creatures dealing combat damage in the same step draw only one card")
    void batchedTriggerDrawsOnlyOnce() {
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        addAttacker(new Memnite());
        addAttacker(new Memnite());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Drain every trigger the batch produced — it must add up to a single card.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A non-artifact creature dealing combat damage does not draw")
    void nonArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        addAttacker(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("An artifact creature dealing no combat damage does not draw")
    void zeroPowerArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThopterSpyNetwork());
        addAttacker(new Ornithopter());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
