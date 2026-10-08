package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.d.DragonlordDromoka;
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

@CardUsed({WrathfulRedDragon.class, DragonEgg.class, DragonlordDromoka.class, GrizzlyBears.class, Shock.class})
class WrathfulRedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("A Dragon dealt damage causes that much damage to a non-Dragon target")
    void dragonDamageIsReflected() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player1, new DragonEgg());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dragon Egg"));
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

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

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
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Dragon Egg"));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bears.getId(), player2.getId()).doesNotContain(dragonId);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The damaged Dragon deals the reflected damage and applies its lifelink")
    void damagedDragonIsDamageSource() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player1, new DragonlordDromoka());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Dragonlord Dromoka"));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Damage to Wrathful Red Dragon itself triggers and can damage its controller")
    void selfDamageCanTargetController() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Wrathful Red Dragon"));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Wrathful Red Dragon");
    }

    @Test
    @DisplayName("Damage to an opponent's Dragon does not trigger")
    void opponentsDragonDoesNotTrigger() {
        harness.addToBattlefield(player1, new WrathfulRedDragon());
        harness.addToBattlefield(player2, new WrathfulRedDragon());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Wrathful Red Dragon"));
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
