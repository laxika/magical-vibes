package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinationRioter.class, Murder.class, Forest.class, LlanowarElves.class})
class RuinationRioterTest extends BaseCardTest {

    @Test
    @DisplayName("When Ruination Rioter dies, it may deal damage equal to lands in its controller's graveyard")
    void deathTriggerDealsDamageEqualToLandsInGraveyard() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ruination Rioter counts lands but not other cards in its controller's graveyard")
    void deathTriggerCountsOnlyLands() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new LlanowarElves()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining Ruination Rioter's death trigger deals no damage")
    void decliningDeathTriggerDealsNoDamage() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No lands in the controller's graveyard means no damage, even with opposing lands")
    void emptyGraveyardIgnoresOpponentsLands() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Land count is evaluated when the death ability resolves")
    void countsLandsAtResolution() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setGraveyard(player1, List.of(new RuinationRioter(), new Murder(),
                new Forest(), new Forest(), new Forest()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The death ability can deal damage to a creature")
    void canDamageCreature() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setGraveyard(player1, List.of(new Forest()));
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The death ability can target its own controller")
    void canDamageController() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private void destroyRioter(Permanent rioter) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, rioter.getId());
    }
}
