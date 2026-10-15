package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuncturingBlow.class, GrizzlyBears.class, AvatarOfMight.class, Terror.class})
class PuncturingBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a small creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new PuncturingBlow()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 5 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new PuncturingBlow()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(avatar.getMarkedDamage()).isEqualTo(5);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new PuncturingBlow()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature destroyed later in the turn is exiled")
    void laterDestructionExilesSurvivor() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new PuncturingBlow(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, avatar.getId());
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(harness.getGameData().exiledCards)
                .anyMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("The exile replacement expires before destruction on the next turn")
    void replacementExpiresAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new PuncturingBlow(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, avatar.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(harness.getGameData().exiledCards)
                .noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }
}
