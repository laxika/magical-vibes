package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonsPrey;
import com.github.laxika.magicalvibes.cards.f.FrontlineRush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShockingSharpshooter.class, DragonsPrey.class, FrontlineRush.class})
class ShockingSharpshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering deals 1 damage to a target opponent")
    void anotherCreatureEnteringDealsDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ShockingSharpshooter());

        harness.setHand(player1, List.of(new ShockingSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The triggered ability can target only an opponent")
    void triggeredAbilityTargetsOnlyOpponent() {
        harness.addToBattlefield(player1, new ShockingSharpshooter());

        harness.setHand(player1, List.of(new ShockingSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The Sharpshooter does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new ShockingSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger the Sharpshooter")
    void opponentCreatureEnteringDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ShockingSharpshooter());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ShockingSharpshooter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each creature token entering triggers separately")
    void eachTokenEnteringDealsDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ShockingSharpshooter());
        harness.setHand(player1, List.of(new FrontlineRush()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The damage trigger resolves after its source is destroyed")
    void damageResolvesAfterSourceIsDestroyed() {
        harness.setLife(player2, 20);
        var source = harness.addToBattlefieldAndReturn(player1, new ShockingSharpshooter());
        harness.setHand(player1, List.of(new ShockingSharpshooter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.setHand(player2, List.of(new DragonsPrey()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        harness.assertInGraveyard(player1, "Shocking Sharpshooter");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
