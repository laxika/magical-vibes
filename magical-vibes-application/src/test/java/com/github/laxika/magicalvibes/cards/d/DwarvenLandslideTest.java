package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenLandslide.class, Forest.class, GrizzlyBears.class, Mountain.class})
class DwarvenLandslideTest extends BaseCardTest {

    @Test
    void withoutKickerDestroysOneTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new DwarvenLandslide()));
        addBaseMana();

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    void kickedDestroysAnotherTargetLandAndSacrificesALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new DwarvenLandslide()));
        addKickedMana();

        UUID sacrificeId = harness.getPermanentId(player1, "Mountain");
        UUID firstTargetId = harness.getPermanentId(player2, "Forest");
        UUID secondTargetId = harness.getPermanentId(player2, "Mountain");
        harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(firstTargetId, secondTargetId), List.of(), false, sacrificeId,
                null, null, null, null, true
        );
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    void cannotUseNonLandAsKickerSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new DwarvenLandslide()));
        addKickedMana();

        UUID firstTargetId = harness.getPermanentId(player2, "Forest");
        UUID secondTargetId = harness.getPermanentId(player2, "Mountain");
        UUID sacrificeId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(firstTargetId, secondTargetId), List.of(), false, sacrificeId,
                null, null, null, null, true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a land");
    }

    @Test
    void cannotTargetSameLandTwiceWhenKicked() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DwarvenLandslide()));
        addKickedMana();

        UUID targetId = harness.getPermanentId(player2, "Forest");
        UUID sacrificeId = harness.getPermanentId(player1, "Mountain");
        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(targetId, targetId), List.of(), false, sacrificeId,
                null, null, null, null, true
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
