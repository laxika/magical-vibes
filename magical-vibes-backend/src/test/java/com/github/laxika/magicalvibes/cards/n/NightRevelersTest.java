package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NightRevelersTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Night Revelers has correct static effect")
    void hasCorrectProperties() {
        NightRevelers card = new NightRevelers();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);

        var effect = (OpponentControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(effect.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect haste = (GrantKeywordEffect) effect.wrapped();
        assertThat(haste.keywords()).containsExactly(Keyword.HASTE);
        assertThat(haste.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Conditional haste with opponent's Human =====

    @Test
    @DisplayName("Night Revelers has haste when an opponent controls a Human")
    void hasHasteWhenOpponentControlsHuman() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player2, new EliteVanguard()); // Human Soldier

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Night Revelers does NOT have haste when no opponent controls a Human")
    void noHasteWithoutOpponentHuman() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player2, new GrizzlyBears()); // Bear, not Human

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Night Revelers does NOT have haste when only controller controls a Human")
    void noHasteWhenControllerControlsHuman() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player1, new EliteVanguard()); // Own Human — doesn't count

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Night Revelers alone does NOT have haste")
    void noHasteAlone() {
        harness.addToBattlefield(player1, new NightRevelers());

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    // ===== Loses haste when opponent's Human leaves =====

    @Test
    @DisplayName("Night Revelers loses haste when opponent's Human leaves the battlefield")
    void losesHasteWhenOpponentHumanLeaves() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Remove the opponent's Human
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Elite Vanguard"));

        // Haste should be gone immediately (computed on the fly)
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isFalse();
    }

    // ===== Multiple opponent Humans =====

    @Test
    @DisplayName("Losing one opponent Human while another exists still grants haste")
    void stillHasHasteWithMultipleOpponentHumans() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Remove one Human
        Permanent firstVanguard = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elite Vanguard"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player2.getId()).remove(firstVanguard);

        // Still has opponent's Human — haste remains
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static haste survives end-of-turn modifier reset")
    void staticHasteSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new NightRevelers());
        harness.addToBattlefield(player2, new EliteVanguard());

        Permanent revelers = findPermanent(player1, "Night Revelers");

        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();

        // Simulate end-of-turn cleanup
        revelers.resetModifiers();

        // Static haste should still be computed
        assertThat(gqs.hasKeyword(gd, revelers, Keyword.HASTE)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
