package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EidolonOfBlossoms;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrictProctor.class, EidolonOfBlossoms.class, Forest.class, TurnToFrog.class})
class StrictProctorTest extends BaseCardTest {

    @Test
    void countersTriggeredAbilityCausedByPermanentEntering() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player2, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eidolon of Blossoms");
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void controllerOfCausedAbilityMayPayTwoMana() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player2, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void taxesEachAbilityWhenAnEnchantmentTriggersAnExistingPermanent() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.addToBattlefield(player2, new EidolonOfBlossoms());
        harness.setHand(player2, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(4);
    }

    @Test
    void canDeclinePaymentForItsControllersOwnTriggeredAbility() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player1, List.of(new EidolonOfBlossoms()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerAfterLosingAllAbilities() {
        harness.addToBattlefield(player1, new StrictProctor());
        harness.setHand(player2, List.of(new TurnToFrog(), new EidolonOfBlossoms()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Strict Proctor").getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player2, "Forest");
    }
}
