package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FantasticElasticity.class, DarkRitual.class, GrizzlyBears.class, Forest.class})
class FantasticElasticityTest extends BaseCardTest {

    @Test
    @DisplayName("Bounce mode returns a target nonland permanent to its owner's hand")
    void bouncesNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Graveyard mode returns an instant or sorcery card to hand")
    void returnsInstantOrSorceryFromGraveyard() {
        Card spell = new DarkRitual();
        harness.setGraveyard(player1, List.of(spell));

        cast(1, spell.getId());

        harness.assertInHand(player1, "Dark Ritual");
        harness.assertNotInGraveyard(player1, "Dark Ritual");
    }

    @Test
    @DisplayName("Bounce mode cannot target a land")
    void bounceModeRejectsLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FantasticElasticity()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard mode cannot target a creature card")
    void graveyardModeRejectsCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new FantasticElasticity()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rebound exiles the spell and schedules a cast at the next upkeep")
    void reboundSchedulesNextUpkeepCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        FantasticElasticity card = new FantasticElasticity();
        harness.setHand(player1, List.of(card));
        addMana();

        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FantasticElasticity()));
        addMana();
        harness.castSorcery(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
