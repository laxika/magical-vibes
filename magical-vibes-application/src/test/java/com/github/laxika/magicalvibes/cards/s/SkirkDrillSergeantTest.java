package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoblinGrappler;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkirkDrillSergeant.class, GoblinGrappler.class, FugitiveWizard.class, Shock.class})
class SkirkDrillSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Another Goblin dying triggers the paid reveal")
    void anotherGoblinDyingTriggersAbility() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        Card topGoblin = new GoblinGrappler();
        harness.setLibrary(player1, List.of(topGoblin));
        addShockMana(player1);

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topGoblin.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(topGoblin.getId()));
    }

    @Test
    @DisplayName("Its own death triggers the paid reveal")
    void ownDeathTriggersAbility() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        Card topGoblin = new GoblinGrappler();
        harness.setLibrary(player1, List.of(topGoblin));
        addShockMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);

        killCreature(player2, player1, "Skirk Drill Sergeant");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topGoblin.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Goblin top permanent card goes to the graveyard")
    void nonGoblinTopPermanentGoesToGraveyard() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        Card topNonGoblin = new FugitiveWizard();
        harness.setLibrary(player1, List.of(topNonGoblin));
        addShockMana(player1);

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topNonGoblin.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topNonGoblin.getId()));
    }

    @Test
    @DisplayName("A nonpermanent top card goes to the graveyard")
    void nonPermanentTopCardGoesToGraveyard() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        Card topInstant = new Shock();
        harness.setLibrary(player1, List.of(topInstant));
        addShockMana(player1);

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topInstant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topInstant.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves the library unchanged")
    void decliningPaymentLeavesLibraryUnchanged() {
        addShockMana(player1);
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        Card topGoblin = new GoblinGrappler();
        harness.setLibrary(player1, List.of(topGoblin));

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topGoblin);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topGoblin.getId()));
    }

    @Test
    @DisplayName("A non-Goblin death does not trigger")
    void nonGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new FugitiveWizard());
        Card topGoblin = new GoblinGrappler();
        harness.setLibrary(player1, List.of(topGoblin));
        harness.addMana(player1, ManaColor.RED, 1);

        killCreature(player1, player2, "Fugitive Wizard");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topGoblin);
    }

    private void addShockMana(Player player) {
        harness.addMana(player, ManaColor.RED, 4);
    }

    private void killCreature(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
