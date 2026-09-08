package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythosOfSnapdax.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class MythosOfSnapdaxTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses their own permanents when black and red mana were not spent")
    void eachPlayerChoosesTheirOwnPermanents() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castWithMana(ManaColor.COLORLESS, ManaColor.COLORLESS);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentGiant.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The controller chooses for every player when black and red mana were spent")
    void controllerChoosesForEveryPlayerWithBlackAndRedMana() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castWithMana(ManaColor.BLACK, ManaColor.RED);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentGiant.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private void castWithMana(ManaColor firstExtraColor, ManaColor secondExtraColor) {
        harness.setHand(player1, List.of(new MythosOfSnapdax()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, firstExtraColor, 1);
        harness.addMana(player1, secondExtraColor, 1);
        harness.castSorcery(player1, 0, 0);
    }
}
