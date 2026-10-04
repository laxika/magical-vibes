package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.n.NihilSpellbomb;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmerpointStag.class, MoriokReaver.class, Plains.class, PullFromEternity.class, NihilSpellbomb.class})
class GlimmerpointStagTest extends BaseCardTest {


    @Test
    @DisplayName("Casting with target puts creature spell on stack")
    void castingPutsCreatureOnStack() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving creature spell triggers ETB exile ability")
    void resolvingTriggersEtb() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();

        // Stag should be on battlefield
        harness.assertOnBattlefield(player1, "Glimmerpoint Stag");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }


    @Test
    @DisplayName("ETB exiles the target permanent")
    void etbExilesTargetPermanent() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moriok Reaver");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));
    }

    @Test
    @DisplayName("Exiled permanent returns at beginning of next end step under owner's control")
    void exiledPermanentReturnsAtEndStep() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell + ETB
        resolveAllTriggers();

        // Bears should be exiled
        harness.assertNotOnBattlefield(player2, "Moriok Reaver");

        // Advance to end step
        advanceToEndStep();

        // Bears should be back on battlefield under owner's control
        harness.assertOnBattlefield(player2, "Moriok Reaver");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moriok Reaver"));
    }

    @Test
    @DisplayName("Can exile own permanent and it returns under owner's control")
    void canExileOwnPermanent() {
        harness.addToBattlefield(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell + ETB
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Moriok Reaver");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Moriok Reaver");
    }

    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell â†’ ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB â†’ fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Nothing should be pending to return
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        // Exile own creature so it returns under player1's control;
        // The returned creature is a new permanent during the same turn.
        harness.addToBattlefield(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell + ETB
        resolveAllTriggers();

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Moriok Reaver");
        assertThat(returnedCreature.isSummoningSick()).isTrue();
    }


    @Test
    @DisplayName("Can target another Glimmerpoint Stag")
    void canTargetAnotherStag() {
        harness.addToBattlefield(player2, new GlimmerpointStag());
        UUID targetId = harness.getPermanentId(player2, "Glimmerpoint Stag");
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Glimmerpoint Stag");
        harness.assertOnBattlefield(player1, "Glimmerpoint Stag");
    }

    @Test
    @DisplayName("Can exile a land and return it untapped as a new permanent")
    void canExileLand() {
        harness.addToBattlefield(player2, new Plains());
        Permanent land = findPermanent(player2, "Plains");
        land.tap();
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, land.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Plains");
        advanceToEndStep();

        Permanent returned = findPermanent(player2, "Plains");
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stolen permanent returns to its owner even after Stag leaves")
    void stolenPermanentReturnsToOwnerAfterSourceLeaves() {
        harness.addToBattlefield(player1, new MoriokReaver());
        Permanent target = findPermanent(player1, "Moriok Reaver");
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Glimmerpoint Stag"));
        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Moriok Reaver");
        harness.assertOnBattlefield(player2, "Moriok Reaver");
    }

    @Test
    @DisplayName("Delayed return cannot return a card that left exile and was exiled again")
    @CardUsed({GlimmerpointStag.class, MoriokReaver.class, PullFromEternity.class, NihilSpellbomb.class})
    void doesNotReturnNewExileObject() {
        MoriokReaver target = new MoriokReaver();
        harness.addToBattlefield(player2, target);
        harness.setHand(player1, List.of(new GlimmerpointStag()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Moriok Reaver"));
        resolveAllTriggers();
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Moriok Reaver");

        harness.addToBattlefield(player1, new NihilSpellbomb());
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        advanceToEndStep();

        harness.assertNotOnBattlefield(player2, "Moriok Reaver");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }
    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
