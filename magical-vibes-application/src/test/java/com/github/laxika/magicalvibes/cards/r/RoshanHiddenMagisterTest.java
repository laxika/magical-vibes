package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoshanHiddenMagister.class, GrizzlyBears.class, Forest.class})
class RoshanHiddenMagisterTest extends BaseCardTest {

    @Test
    void grantsAssassinToOtherControlledCreaturesAndOwnedCreatureCardsOutsideBattlefield() {
        addCreatureReady(player1, new RoshanHiddenMagister());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears handCreature = new GrizzlyBears();
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        GrizzlyBears libraryCreature = new GrizzlyBears();
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(handCreature));
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setExile(player1, List.of(exiledCreature));

        assertThat(gqs.hasEffectiveSubtype(gd, ownCreature, CardSubtype.ASSASSIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentCreature, CardSubtype.ASSASSIN)).isFalse();
        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.ASSASSIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardCreature, CardSubtype.ASSASSIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(libraryCreature, CardSubtype.ASSASSIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(exiledCreature, CardSubtype.ASSASSIN, gd, player1.getId())).isTrue();
    }

    @Test
    void givesMenaceOnlyToControlledFaceDownCreatures() {
        addCreatureReady(player1, new RoshanHiddenMagister());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent faceUp = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentFaceDown = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentFaceDown.setFaceDownAsCloaked();

        assertThat(gqs.hasKeyword(gd, faceDown, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, faceUp, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentFaceDown, Keyword.MENACE)).isFalse();
    }

    @Test
    void drawsAndLosesLifeWhenAControlledPermanentTurnsFaceUp() {
        addCreatureReady(player1, new RoshanHiddenMagister());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        faceDown.setFaceDownAsCloaked();
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.getLife(player1.getId());

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDown);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
