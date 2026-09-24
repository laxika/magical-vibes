package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.cards.v.VulshokWarBoar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtDissector.class, DarksteelIngot.class, VulshokWarBoar.class,
        EchoingDecay.class, BarbedLightning.class})
class ThoughtDissectorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first revealed artifact under the controller's control, sacrifices itself, and mills the rest")
    void stealsFirstArtifactAndMillsEarlierCards() {
        addReadyThoughtDissector();
        Card milled = new VulshokWarBoar();
        Card artifact = new DarksteelIngot();
        Card remains = new BarbedLightning();
        harness.setLibrary(player2, List.of(milled, artifact, remains));

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Thought Dissector");
        harness.assertInGraveyard(player2, "Vulshok War Boar");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remains);
    }

    @Test
    @DisplayName("Stops after X cards when no artifact is revealed")
    void stopsAtXWithoutArtifact() {
        addReadyThoughtDissector();
        Card first = new VulshokWarBoar();
        Card second = new EchoingDecay();
        Card remains = new BarbedLightning();
        harness.setLibrary(player2, List.of(first, second, remains));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Dissector");
        harness.assertNotInGraveyard(player1, "Thought Dissector");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remains);
    }

    @Test
    @DisplayName("Does not reveal an artifact after X non-artifact cards")
    void ignoresArtifactAfterXCards() {
        addReadyThoughtDissector();
        Card first = new VulshokWarBoar();
        Card second = new EchoingDecay();
        Card artifact = new DarksteelIngot();
        harness.setLibrary(player2, List.of(first, second, artifact));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Dissector");
        harness.assertNotOnBattlefield(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player2, "Vulshok War Boar");
        harness.assertInGraveyard(player2, "Echoing Decay");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("Allows X equal to zero without revealing any cards")
    void zeroXLeavesSourceAndLibraryInPlace() {
        addReadyThoughtDissector();
        Card artifact = new DarksteelIngot();
        harness.setLibrary(player2, List.of(artifact));

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Dissector");
        harness.assertNotInGraveyard(player1, "Thought Dissector");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only an opponent can be targeted")
    void rejectsSelfAsTarget() {
        addReadyThoughtDissector();
        harness.setLibrary(player2, List.of(new DarksteelIngot()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyThoughtDissector() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ThoughtDissector());
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
