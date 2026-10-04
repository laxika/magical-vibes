package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeedTheFlames.class, GrizzlyBears.class, AvatarOfMight.class, WrathOfGod.class})
class FeedTheFlamesTest extends BaseCardTest {

    @Test
    void lethalDamageExilesTheCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new FeedTheFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

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

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    void laterDestructionExilesOnlyTheMarkedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeedTheFlames(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Avatar of Might"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    void destructionOnTheNextTurnGoesToTheGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new FeedTheFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player2, 0, (UUID) null);

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getName().equals("Avatar of Might"));
    }
}
