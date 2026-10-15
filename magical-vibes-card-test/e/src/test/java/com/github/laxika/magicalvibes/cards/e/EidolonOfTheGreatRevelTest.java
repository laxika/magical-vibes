package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EidolonOfTheGreatRevel.class, Concentrate.class, Divination.class,
        Fireball.class, Fog.class, MagmaSpray.class})
class EidolonOfTheGreatRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to its controller when they cast a spell with mana value 3 or less")
    void damagesControllerCastingLowManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to the player who casts a spell with mana value 3 or less")
    void damagesOpponentCastingLowManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for a spell with mana value 4 or greater")
    void doesNotTriggerForHighManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Concentrate()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Uses the chosen X value when checking a spell's mana value")
    void usesChosenXValueForManaValueCheck() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Casting Eidolon does not trigger its own ability")
    void doesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new EidolonOfTheGreatRevel()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Eidolon of the Great Revel");
    }

    @Test
    @DisplayName("An existing Eidolon triggers for another Eidolon before the creature spell resolves")
    void triggersForCreatureSpellBeforeItResolves() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new EidolonOfTheGreatRevel()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Eidolon of the Great Revel");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eidolon of the Great Revel");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Eidolon independently damages the caster")
    void multipleEidolonsDamageTheSameCaster() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.addToBattlefield(player2, new EidolonOfTheGreatRevel());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An X spell with mana value exactly three triggers Eidolon")
    void triggersForXSpellAtManaValueThree() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 2, player1.getId());

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A queued trigger still deals damage after Eidolon leaves the battlefield")
    void triggerSurvivesSourceRemoval() {
        var eidolon = harness.addToBattlefieldAndReturn(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player1, List.of(new Fog()));
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        gs.passPriority(gd, player1);
        harness.castAndResolveInstant(player2, 0, eidolon.getId());
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Eidolon of the Great Revel");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
