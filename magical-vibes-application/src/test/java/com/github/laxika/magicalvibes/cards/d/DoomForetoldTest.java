package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.cards.t.TrueLovesKiss;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoomForetold.class, Forest.class, RovingKeep.class, TrueLovesKiss.class, LeylineOfSanctity.class})
class DoomForetoldTest extends BaseCardTest {

    @Test
    @DisplayName("The active player sacrifices a nonland, nontoken permanent when able")
    void sacrificesMatchingPermanent() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoomForetold());
        Permanent keep = harness.addToBattlefieldAndReturn(player2, new RovingKeep());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(keep);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doom);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Knight")).isZero();
    }

    @Test
    @DisplayName("The active player chooses which matching permanent to sacrifice")
    void choosesMatchingPermanent() {
        harness.addToBattlefield(player1, new DoomForetold());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RovingKeep());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("If the active player cannot sacrifice, the fallback effects resolve in order")
    void resolvesFallbackWhenNoMatchingPermanentExists() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoomForetold());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new RovingKeep()));
        int initialControllerHandSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(initialControllerHandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doom);
        assertThat(countPermanents(player1, "Knight")).isOne();
        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("During its controller's upkeep, Doom Foretold can be sacrificed to its own ability")
    void sacrificesItselfWhenItIsTheOnlyMatchingPermanent() {
        harness.addToBattlefield(player1, new DoomForetold());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doom Foretold");
        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Knight")).isZero();
    }

    @Test
    @DisplayName("Player hexproof does not prevent the mandatory sacrifice")
    void hexproofDoesNotPreventSacrifice() {
        harness.addToBattlefield(player1, new DoomForetold());
        harness.addToBattlefield(player2, new LeylineOfSanctity());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leyline of Sanctity");
        harness.assertOnBattlefield(player1, "Doom Foretold");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An empty hand does not stop the fallback effects")
    void emptyHandDoesNotStopFallback() {
        harness.addToBattlefield(player1, new DoomForetold());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Doom Foretold");
        assertThat(countPermanents(player1, "Knight")).isOne();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Knight token cannot be sacrificed to avoid the fallback")
    void tokensDoNotQualifyForSacrifice() {
        harness.addToBattlefield(player2, new DoomForetold());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent knight = findPermanent(player2, "Knight");

        harness.addToBattlefield(player1, new DoomForetold());
        harness.setHand(player2, List.of());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(knight);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Doom Foretold");
        assertThat(countPermanents(player1, "Knight")).isOne();
    }

    @Test
    @DisplayName("The controller's fallback still resolves after Doom Foretold is exiled")
    void ownUpkeepFallbackResolvesAfterSourceLeaves() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new DoomForetold());
        harness.setHand(player1, List.of(new TrueLovesKiss()));
        harness.setLibrary(player1, List.of(new Forest(), new RovingKeep()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0, doom.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Doom Foretold");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Roving Keep");
        assertThat(countPermanents(player1, "Knight")).isOne();
        harness.assertNotInGraveyard(player1, "Doom Foretold");
    }
}
