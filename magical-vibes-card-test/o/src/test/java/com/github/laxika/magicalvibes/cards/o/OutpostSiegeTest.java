package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PerilousVault;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutpostSiege.class, GrizzlyBears.class, Murder.class, PerilousVault.class})
class OutpostSiegeTest extends BaseCardTest {

    private void castSiege(String mode) {
        harness.castFromHand(player1, new OutpostSiege(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    @Test
    @DisplayName("Khans exiles the top card during upkeep and lets the controller play it")
    void khansExilesTopCardDuringUpkeep() {
        Card top = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        castSiege("Khans");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Dragons deals 1 damage to any target when your creature leaves")
    void dragonsDealsDamageWhenOwnCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        castSiege("Dragons");

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Dragons does not trigger when an opponent's creature leaves")
    void dragonsDoesNotTriggerForOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege("Dragons");

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Khans does not trigger when your creature leaves")
    void khansDoesNotTriggerWhenOwnCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSiege("Khans");

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void khansAllowsCastingExiledCreatureWithItsNormalManaCost() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, new GrizzlyBears()));
        castSiege("Khans");
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void khansDoesNotExileDuringOpponentsUpkeep() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        castSiege("Khans");

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void dragonsDoesNotExileDuringUpkeep() {
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        castSiege("Dragons");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void khansDoesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castSiege("Khans");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void dragonsTriggersWhenSiegeAndCreatureAreExiledTogether() {
        castSiege("Dragons");
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PerilousVault());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Outpost Siege");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
