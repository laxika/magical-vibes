package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrackTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with exactly one permanent loses it automatically")
    void eachPlayerWithOnePermanentLosesIt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new CrackTheEarth()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A player with several permanents chooses which one to sacrifice")
    void playerWithSeveralPermanentsChooses() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CrackTheEarth()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Any permanent type can be sacrificed, including lands")
    void anyPermanentTypeCanBeSacrificed() {
        harness.addToBattlefield(player2, new Mountain());

        harness.setHand(player1, List.of(new CrackTheEarth()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("A player with no permanents is unaffected")
    void playerWithNoPermanentsIsUnaffected() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new CrackTheEarth()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
    }
}
