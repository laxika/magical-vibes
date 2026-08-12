package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThoughtDissectorTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first revealed artifact under the controller's control, sacrifices itself, and mills the rest")
    void stealsFirstArtifactAndMillsEarlierCards() {
        addReadyThoughtDissector();
        Card milled = new GrizzlyBears();
        Card artifact = new DarksteelIngot();
        Card remains = new Shock();
        harness.setLibrary(player2, List.of(milled, artifact, remains));

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Thought Dissector");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remains);
    }

    @Test
    @DisplayName("Stops after X cards when no artifact is revealed")
    void stopsAtXWithoutArtifact() {
        addReadyThoughtDissector();
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card remains = new Swamp();
        harness.setLibrary(player2, List.of(first, second, remains));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Dissector");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Thought Dissector"));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remains);
    }

    @Test
    @DisplayName("Only an opponent can be targeted")
    void rejectsSelfAsTarget() {
        addReadyThoughtDissector();
        harness.setLibrary(player2, List.of(new DarksteelIngot()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyThoughtDissector() {
        harness.addToBattlefield(player1, new ThoughtDissector());
        Permanent source = findPermanent(player1, "Thought Dissector");
        source.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return source;
    }
}
