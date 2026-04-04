package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegisaurAlphaTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has static GrantKeywordEffect for haste and ETB token effect")
    void hasCorrectEffects() {
        RegisaurAlpha card = new RegisaurAlpha();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(grant.keywords()).containsExactly(Keyword.HASTE);
        assertThat(grant.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(grant.filter()).isNotNull();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Dinosaur");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(3);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.TRAMPLE);
    }

    // ===== ETB: creates a 3/3 green Dinosaur token with trample =====

    @Test
    @DisplayName("ETB creates a 3/3 green Dinosaur token with trample")
    void etbCreatesDinosaurToken() {
        castAndResolveAlpha();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Alpha + 1 token
        assertThat(countDinosaurTokens(player1)).isEqualTo(1);

        Permanent token = findDinosaurToken(player1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Dinosaur token created by ETB has haste from Regisaur Alpha's static ability")
    void tokenHasHasteFromStaticAbility() {
        castAndResolveAlpha();

        Permanent token = findDinosaurToken(player1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    // ===== Static effect: grants haste to other Dinosaurs you control =====

    @Test
    @DisplayName("Other Dinosaurs you control have haste")
    void grantsHasteToOtherDinosaurs() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent raptor = findPermanent(player1, "Raptor Companion");
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Regisaur Alpha does not grant haste to itself")
    void doesNotGrantHasteToItself() {
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent alpha = findPermanent(player1, "Regisaur Alpha");
        // Regisaur Alpha is a Dinosaur but should not grant haste to itself (OWN_CREATURES excludes self)
        assertThat(gqs.hasKeyword(gd, alpha, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to non-Dinosaur creatures")
    void doesNotGrantHasteToNonDinosaurs() {
        harness.addToBattlefield(player1, new RegisaurAlpha());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to opponent's Dinosaurs")
    void doesNotGrantHasteToOpponentDinosaurs() {
        harness.addToBattlefield(player1, new RegisaurAlpha());
        harness.addToBattlefield(player2, new RaptorCompanion());

        Permanent opponentRaptor = findPermanent(player2, "Raptor Companion");
        assertThat(gqs.hasKeyword(gd, opponentRaptor, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not give P/T boost — only grants haste")
    void doesNotBoostPowerToughness() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        Permanent raptor = findPermanent(player1, "Raptor Companion");
        // Raptor Companion is 3/1 base — should remain 3/1
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(1);
    }

    // ===== Haste removed when Regisaur Alpha leaves =====

    @Test
    @DisplayName("Haste is removed when Regisaur Alpha leaves the battlefield")
    void hasteRemovedWhenAlphaLeaves() {
        harness.addToBattlefield(player1, new RegisaurAlpha());
        harness.addToBattlefield(player1, new RaptorCompanion());

        Permanent raptor = findPermanent(player1, "Raptor Companion");
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Regisaur Alpha"));

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HASTE)).isFalse();
    }

    // ===== Two Regisaur Alphas =====

    @Test
    @DisplayName("Two Regisaur Alphas grant haste to each other")
    void twoAlphasGrantHasteToEachOther() {
        harness.addToBattlefield(player1, new RegisaurAlpha());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        List<Permanent> alphas = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Regisaur Alpha"))
                .toList();

        assertThat(alphas).hasSize(2);
        for (Permanent alpha : alphas) {
            assertThat(gqs.hasKeyword(gd, alpha, Keyword.HASTE)).isTrue();
        }
    }

    // ===== Helpers =====

    private void castAndResolveAlpha() {
        harness.setHand(player1, List.of(new RegisaurAlpha()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countDinosaurTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dinosaur"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.DINOSAUR))
                .count();
    }

    private Permanent findDinosaurToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dinosaur"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Dinosaur token found"));
    }

}
