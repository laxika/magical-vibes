package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LozhanDragonsLegacy.class, DragonEgg.class, YoungRedDragon.class,
        GrizzlyBears.class})
class LozhanDragonsLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Dragon deals damage equal to its mana value")
    void dragonSpellDealsDamageEqualToManaValue() {
        addLozhan();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Casting an Adventure deals damage equal to the Adventure spell's mana value")
    void adventureSpellDealsDamageEqualToAdventureManaValue() {
        addLozhan();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new YoungRedDragon()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A commander is not a legal target")
    void commanderIsNotLegalTarget() {
        addLozhan();
        Permanent commander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        commander.setCommander(true);
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(commander.getId());
    }

    @Test
    @DisplayName("Casting a non-Dragon non-Adventure spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        addLozhan();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting the creature half of an Adventure Dragon triggers only once")
    void adventureDragonCreatureTriggersOnce() {
        addLozhan();
        harness.setHand(player1, List.of(new YoungRedDragon()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Young Red Dragon");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Lozhan can damage a noncommander creature")
    void damagesNonCommanderCreature() {
        addLozhan();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YoungRedDragon());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Young Red Dragon");
        harness.assertInGraveyard(player2, "Young Red Dragon");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting an Adventure does not trigger Lozhan")
    void opponentAdventureDoesNotTrigger() {
        addLozhan();
        harness.setHand(player2, List.of(new YoungRedDragon()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.ensurePriority(player2);

        harness.castAdventure(player2, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addLozhan() {
        harness.addToBattlefield(player1, new LozhanDragonsLegacy());
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
    }
}
