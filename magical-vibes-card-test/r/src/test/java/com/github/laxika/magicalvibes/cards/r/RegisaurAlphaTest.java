package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.cards.u.UnfriendlyFire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegisaurAlpha.class, RaptorCompanion.class, QueensBaySoldier.class, UnfriendlyFire.class})
class RegisaurAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/3 green Dinosaur token with trample")
    void etbCreatesDinosaurToken() {
        castAndResolveAlpha();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Alpha + 1 token
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);

        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.GREEN);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Dinosaur token created by ETB has haste from Regisaur Alpha's static ability")
    void tokenHasHasteFromStaticAbility() {
        castAndResolveAlpha();

        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

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
        harness.addToBattlefield(player1, new QueensBaySoldier());

        Permanent soldier = findPermanent(player1, "Queen's Bay Soldier");
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.HASTE)).isFalse();
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

    @Test
    @DisplayName("Two Regisaur Alphas grant haste to each other")
    void twoAlphasGrantHasteToEachOther() {
        harness.addToBattlefield(player1, new RegisaurAlpha());
        harness.addToBattlefield(player1, new RegisaurAlpha());

        List<Permanent> alphas = findPermanents(player1, "Regisaur Alpha");

        assertThat(alphas).hasSize(2);
        for (Permanent alpha : alphas) {
            assertThat(gqs.hasKeyword(gd, alpha, Keyword.HASTE)).isTrue();
        }
    }

    @Test
    @DisplayName("ETB still creates a token after Alpha is destroyed in response")
    void tokenCreatedAfterAlphaLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new RegisaurAlpha()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Regisaur Alpha");
        assertThat(countPermanents(player1, "Dinosaur")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0,
                findPermanent(player1, "Regisaur Alpha").getId());
        harness.assertNotOnBattlefield(player1, "Regisaur Alpha");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("The new Dinosaur token can attack immediately with Alpha's haste")
    void tokenCanAttackTheTurnItEnters() {
        castAndResolveAlpha();

        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(token.isSummoningSick()).isTrue();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        declareAttackers(List.of(tokenIndex));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    private void castAndResolveAlpha() {
        harness.setHand(player1, List.of(new RegisaurAlpha()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

}
