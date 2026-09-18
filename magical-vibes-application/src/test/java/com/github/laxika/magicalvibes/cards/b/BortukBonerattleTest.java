package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BortukBonerattle.class, AirElemental.class, Forest.class, Island.class, GrizzlyBears.class, Zombify.class})
class BortukBonerattleTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with mana value at most Domain to the battlefield")
    void returnsCreatureToBattlefieldWhenWithinDomain() {
        Card target = new GrizzlyBears();
        castBortuk(target);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a creature above Domain to its owner's hand")
    void returnsCreatureToHandWhenAboveDomain() {
        Card target = new AirElemental();
        castBortuk(target);

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Does not trigger when put onto the battlefield without being cast")
    void doesNotTriggerWhenNotCast() {
        Card bortuk = new BortukBonerattle();
        harness.setGraveyard(player1, List.of(bortuk));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, bortuk.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Bortuk Bonerattle");
    }

    private void castBortuk(Card target) {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BortukBonerattle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
