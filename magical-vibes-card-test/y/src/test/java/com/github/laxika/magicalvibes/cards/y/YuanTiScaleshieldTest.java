package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YuanTiScaleshield.class, Forest.class, GrizzlyBears.class, HillGiant.class, Fireball.class})
class YuanTiScaleshieldTest extends BaseCardTest {

    @Test
    void grantsHexproofAndIndestructibleToAllControlledPermanentsUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScaleshield();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void seeksACreatureAfterAnOpponentCastsASpellWithManaValueAtMostThree() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        castScaleshield();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void doesNotSeekAfterAnOpponentCastsASpellWithManaValueGreaterThanThree() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        castScaleshield();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotSeekWithoutAnOpponentCastingASpell() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castScaleshield();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotGrantKeywordsToPermanentsEnteringAfterResolution() {
        castScaleshield();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent laterLand = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterLand, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void seeksWhileTheQualifyingOpponentSpellIsStillOnTheStack() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.setHand(player1, List.of(new YuanTiScaleshield()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void doesNotSeekAfterAnOpponentCastsFireballWithManaValueFive() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 4, player1.getId());
        harness.passBothPriorities();

        castScaleshield();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castScaleshield() {
        harness.setHand(player1, List.of(new YuanTiScaleshield()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0);
    }
}
