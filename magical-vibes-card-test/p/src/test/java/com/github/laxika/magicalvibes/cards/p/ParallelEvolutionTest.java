package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.s.SkywingAven;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParallelEvolution.class, BaskingRootwalla.class, SkywingAven.class, CabalCoffers.class, SoulWarden.class})
class ParallelEvolutionTest extends BaseCardTest {

    private void addToken(Player player, Card card) {
        card.setToken(true);
        harness.addToBattlefield(player, card);
    }

    private List<Permanent> findTokens(Player player, String name) {
        return findPermanents(player, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private long countTokens(Player player, String name) {
        return findTokens(player, name).size();
    }

    @Test
    @DisplayName("Copies each creature token on every battlefield, but not nontokens or noncreature tokens")
    void copiesCreatureTokensOnEveryBattlefield() {
        addToken(player1, new BaskingRootwalla());
        addToken(player2, new SkywingAven());
        addToken(player1, new CabalCoffers());
        harness.addToBattlefield(player1, new BaskingRootwalla());

        harness.castFromHand(player1, new ParallelEvolution(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(countTokens(player1, "Basking Rootwalla")).isEqualTo(2);
        assertThat(findPermanents(player1, "Basking Rootwalla")).hasSize(3);
        assertThat(findTokens(player1, "Basking Rootwalla")).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
        assertThat(countTokens(player2, "Skywing Aven")).isEqualTo(2);
        assertThat(findTokens(player2, "Skywing Aven")).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        assertThat(countTokens(player1, "Cabal Coffers")).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback copies creature tokens and exiles Parallel Evolution")
    void flashbackCopiesCreatureTokensAndExilesSpell() {
        addToken(player1, new BaskingRootwalla());
        harness.setGraveyard(player1, List.of(new ParallelEvolution()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(countTokens(player1, "Basking Rootwalla")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Parallel Evolution");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Parallel Evolution"));
    }

    @Test
    @DisplayName("Resolves without creating tokens when there are no creature tokens")
    void resolvesWithoutCreatureTokens() {
        harness.addToBattlefield(player1, new BaskingRootwalla());
        addToken(player2, new CabalCoffers());

        harness.castFromHand(player1, new ParallelEvolution(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Parallel Evolution");
    }

    @Test
    @DisplayName("Copies abilities but not a token's temporary pump or tapped status")
    void copiesAbilitiesWithoutTemporaryPumpOrTappedStatus() {
        addToken(player1, new BaskingRootwalla());
        Permanent original = findPermanent(player1, "Basking Rootwalla");
        original.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new ParallelEvolution(), "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent copy = findTokens(player1, "Basking Rootwalla").stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(original.getEffectivePower()).isEqualTo(3);
        assertThat(original.isTapped()).isTrue();
        assertThat(copy.getEffectivePower()).isEqualTo(1);
        assertThat(copy.getEffectiveToughness()).isEqualTo(1);
        assertThat(copy.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, null);
        harness.passBothPriorities();

        assertThat(copy.getEffectivePower()).isEqualTo(3);
        assertThat(copy.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies entering for different players see each other's entry triggers")
    void copiesEnterSimultaneouslyAcrossControllers() {
        addToken(player1, new SoulWarden());
        addToken(player2, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ParallelEvolution(), "{3}{G}{G}");
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countTokens(player1, "Soul Warden")).isEqualTo(2);
        assertThat(countTokens(player2, "Soul Warden")).isEqualTo(2);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }
}
