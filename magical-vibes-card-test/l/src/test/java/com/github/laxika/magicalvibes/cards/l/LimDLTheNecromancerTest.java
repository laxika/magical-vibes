package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CorpulentCorpse;
import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.cards.s.StranglingSoot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LimDLTheNecromancer.class, CorpulentCorpse.class, DrudgeReavers.class, StranglingSoot.class})
class LimDLTheNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the death trigger returns the opponent's creature as a Zombie")
    void payingDeathTriggerReturnsCreatureAsZombie() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent reavers = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, reavers.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Drudge Reavers");
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        harness.assertNotInGraveyard(player2, "Drudge Reavers");
        harness.assertInGraveyard(player1, "Strangling Soot");
    }

    @Test
    @DisplayName("Declining the death trigger leaves the opponent's creature in its graveyard")
    void decliningDeathTriggerLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent reavers = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, reavers.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Drudge Reavers");
        harness.assertNotOnBattlefield(player1, "Drudge Reavers");
    }

    @Test
    @DisplayName("The death trigger does not trigger for a creature you control")
    void ownCreatureDeathDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent reavers = harness.addToBattlefieldAndReturn(player1, new DrudgeReavers());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, reavers.getId());

        harness.assertInGraveyard(player1, "Drudge Reavers");
        harness.assertNotOnBattlefield(player1, "Drudge Reavers");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The activated ability targets only Zombies")
    void regenerateAbilityTargetsOnlyZombies() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent nonZombie = harness.addToBattlefieldAndReturn(player1, new DrudgeReavers());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new CorpulentCorpse());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonZombie.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, zombie.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, zombie.getId());

        harness.assertOnBattlefield(player2, "Corpulent Corpse");
        assertThat(findPermanent(player2, "Corpulent Corpse").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The death trigger goes on the stack before the payment choice")
    void paymentIsChosenOnlyWhenDeathTriggerResolves() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent reavers = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, reavers.getId());

        harness.assertInGraveyard(player2, "Drudge Reavers");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drudge Reavers");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns your own card when it dies under an opponent's control")
    void returnsOwnedCardThatOpponentControlled() {
        harness.addToBattlefield(player1, new LimDLTheNecromancer());
        Permanent reavers = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        gd.stolenCreatures.put(reavers.getId(), player1.getId());
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, reavers.getId());
        harness.assertInGraveyard(player1, "Drudge Reavers");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drudge Reavers");
        harness.assertNotInGraveyard(player1, "Drudge Reavers");
        assertThat(findPermanent(player1, "Drudge Reavers").getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }
}
