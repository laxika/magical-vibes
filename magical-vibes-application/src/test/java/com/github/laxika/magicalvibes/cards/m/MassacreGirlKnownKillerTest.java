package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassacreGirlKnownKiller.class, GrizzlyBears.class, Shock.class})
class MassacreGirlKnownKillerTest extends BaseCardTest {

    @Test
    @DisplayName("Massacre Girl grants wither to creatures you control")
    void grantsWitherToCreaturesYouControl() {
        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, killer, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Draws when an opponent creature dies with toughness less than 1")
    void drawsWhenOpponentCreatureDiesWithToughnessLessThanOne() {
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent killer = addCreatureReady(player1, new MassacreGirlKnownKiller());
        killer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw when an opponent creature dies with toughness greater than 0")
    void doesNotDrawWhenOpponentCreatureDiesWithToughnessGreaterThanZero() {
        harness.setHand(player1, List.of(new Shock()));
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);

        addCreatureReady(player1, new MassacreGirlKnownKiller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
