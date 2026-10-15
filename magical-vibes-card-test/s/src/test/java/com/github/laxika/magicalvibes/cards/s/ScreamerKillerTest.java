package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.Tervigon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamerKiller.class, ColossalDreadmaw.class, HillGiant.class, GrizzlyBears.class,
        Tervigon.class, BlasphemousAct.class})
class ScreamerKillerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a chosen player when you cast a creature with mana value 5 or greater")
    void highManaValueCreatureSpellDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Deals 5 damage to a creature chosen as the target")
    void highManaValueCreatureSpellDealsDamageToCreature() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell with mana value less than 5")
    void lowManaValueCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void chosenXCountsTowardManaValueFive() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Tervigon");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Tervigon");
    }

    @Test
    void chosenXBelowManaValueFiveDoesNotTrigger() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new Tervigon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    void exactlyFiveManaValueTriggersAndCanTargetController() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new ScreamerKiller()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 15);
    }

    @Test
    void doesNotTriggerForItsOwnCastWithoutAnotherScreamerKiller() {
        harness.setHand(player1, List.of(new ScreamerKiller()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Screamer-Killer");
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ScreamerKiller()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void highManaValueNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ScreamerKiller());
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Screamer-Killer");
    }
}
