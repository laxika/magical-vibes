package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeedTheFlamesTest extends BaseCardTest {

    @Test
    void lethalDamageExilesTheCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new FeedTheFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gameData.exiledCards)
                .anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    void survivingCreatureIsMarkedForExileIfItDiesLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new FeedTheFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }
}
