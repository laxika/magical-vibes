package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GoblinGrappler;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkirkDrillSergeant.class, GoblinGrappler.class, FugitiveWizard.class, Shock.class,
        BoggartShenanigans.class})
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

    @Test
    @DisplayName("A noncreature Goblin dying also triggers the paid reveal")
    void noncreatureGoblinDyingTriggersAbility() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        Permanent goblinEnchantment = harness.addToBattlefieldAndReturn(player2, new BoggartShenanigans());
        Card topGoblin = new GoblinGrappler();
        harness.setLibrary(player1, List.of(topGoblin));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, goblinEnchantment));

        harness.assertInGraveyard(player2, "Boggart Shenanigans");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Goblin Grappler");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A Goblin enchantment revealed from the library enters the battlefield")
    void noncreatureGoblinPermanentEntersBattlefield() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        Card topGoblin = new BoggartShenanigans();
        harness.setLibrary(player1, List.of(topGoblin));
        addShockMana(player1);

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertNotInGraveyard(player1, "Boggart Shenanigans");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Payment is still spent when the library is empty")
    void paidRevealWithEmptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new SkirkDrillSergeant());
        harness.addToBattlefield(player2, new GoblinGrappler());
        harness.setLibrary(player1, List.of());
        addShockMana(player1);

        killCreature(player1, player2, "Goblin Grappler");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Skirk Drill Sergeant");
        harness.assertNotOnBattlefield(player1, "Goblin Grappler");
    }

    @Test
    @DisplayName("Simultaneous deaths trigger once for itself and once for the other Goblin")
    void simultaneousGoblinDeathsTriggerSeparately() {
        Permanent sergeant = harness.addToBattlefieldAndReturn(player1, new SkirkDrillSergeant());
        Permanent grappler = harness.addToBattlefieldAndReturn(player1, new GoblinGrappler());
        Card firstGoblin = new GoblinGrappler();
        Card secondGoblin = new SkirkDrillSergeant();
        harness.setLibrary(player1, List.of(firstGoblin, secondGoblin));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(sergeant, grappler), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sergeant);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, grappler);
                }));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstGoblin.getId(), secondGoblin.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
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
