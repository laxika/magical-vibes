package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SymbioteSpiderMan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomDeadlyDevourer.class, SymbioteSpiderMan.class, GrizzlyBears.class, Forest.class})
class VenomDeadlyDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature, then puts counters equal to its toughness on a Symbiote")
    void exilesThenCountersSymbioteUsingToughness() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Permanent symbiote = addCreatureReady(player1, new SymbioteSpiderMan());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCreature);
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, symbiote.getId());
        harness.passBothPriorities();

        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void requiresCreatureCardTarget() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create counters when no Symbiote is available")
    void doesNothingWithoutSymbioteTarget() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCreature);
    }
}
