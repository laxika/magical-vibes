package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathfulRedDragon.class, DragonEgg.class, GrizzlyBears.class, Shock.class})
class WrathfulRedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("A Dragon dealt damage causes that much damage to a non-Dragon target")
    void dragonDamageIsReflected() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player1, new DragonEgg());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Dragon Egg"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to a non-Dragon you control does not trigger Wrathful Red Dragon")
    void nonDragonDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The reflected damage cannot target a Dragon")
    void reflectedDamageExcludesDragons() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player1, new DragonEgg());
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        var dragonId = harness.getPermanentId(player1, "Wrathful Red Dragon");
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Dragon Egg"));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bears.getId(), player2.getId()).doesNotContain(dragonId);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
