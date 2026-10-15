package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InquisitorGreyfax;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarneusCalgar.class, Forest.class, InquisitorGreyfax.class})
class MarneusCalgarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter Master creates two vigilant Astartes Warrior tokens and draws once")
    void createsTokensAndDrawsOnceForTheBatch() {
        Permanent marneus = harness.addToBattlefieldAndReturn(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(marneus), null, null);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> gqs.hasKeyword(gd, token, Keyword.VIGILANCE));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Separate token batches each draw a card, even while Marneus is tapped")
    void drawsForEachSeparateBatchWhileTapped() {
        Permanent marneus = harness.addToBattlefieldAndReturn(player1, new MarneusCalgar());
        marneus.tap();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(marneus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent tokens do not draw cards for Marneus's controller")
    void ignoresOpponentTokens() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.addToBattlefield(player2, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        int ownHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Astartes Warrior")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    @DisplayName("A noncreature Clue token also triggers Master Tactician")
    void drawsForClueToken() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        addCreatureReady(player1, new InquisitorGreyfax());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Nontoken creatures entering do not trigger Master Tactician")
    void ignoresNontokenCreatures() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player1, new InquisitorGreyfax());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Marneus deals damage in both combat damage steps")
    void dealsCombatDamageTwice() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarneusCalgar());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Chapter Master creates untapped 2/2 tokens")
    void createsUntappedTwoTwoTokens() {
        harness.addToBattlefield(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Astartes Warrior")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }
}
