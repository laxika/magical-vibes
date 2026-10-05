package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverchargedAmalgam.class, GrizzlyBears.class, IcyManipulator.class, LightningBolt.class})
class OverchargedAmalgamTest extends BaseCardTest {

    /** Casts Overcharged Amalgam and advances to its exploit "Sacrifice a creature?" prompt. */
    private void castAmalgamToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OverchargedAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> enters, exploit may on stack
        harness.passBothPriorities(); // resolve exploit may -> prompt
    }

    @Test
    @DisplayName("Declining exploit leaves Amalgam on the battlefield")
    void decliningExploitDoesNothing() {
        castAmalgamToExploitPrompt();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Overcharged Amalgam");
    }

    @Test
    @DisplayName("Exploiting another creature counters target spell")
    void exploitOtherCreatureCountersSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        LightningBolt bolt = new LightningBolt();
        OverchargedAmalgam amalgam = new OverchargedAmalgam();
        harness.setHand(player1, List.of(bolt, amalgam));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0); // flash Amalgam in response
        harness.passBothPriorities(); // resolve Amalgam
        harness.passBothPriorities(); // exploit may prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities(); // resolve counter

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Lightning Bolt"));
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Overcharged Amalgam");
    }

    @Test
    @DisplayName("Sacrificing itself for exploit still counters a spell")
    void exploitSelfCountersSpell() {
        LightningBolt bolt = new LightningBolt();
        OverchargedAmalgam amalgam = new OverchargedAmalgam();
        harness.setHand(player1, List.of(bolt, amalgam));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Overcharged Amalgam"));
        harness.handlePermanentChosen(player1, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Lightning Bolt"));
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertNotOnBattlefield(player1, "Overcharged Amalgam");
    }

    @Test
    @DisplayName("Exploit can counter an activated ability")
    void exploitCountersActivatedAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent icy = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, bearsId);

        // Player1 flashes Amalgam in response (same priority window as GlyphKeeper tests)
        harness.setHand(player1, List.of(new OverchargedAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.handlePermanentChosen(player1, icy.getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType().name().contains("ACTIVATED"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));
        Permanent amalgam = findPermanent(player1, "Overcharged Amalgam");
        assertThat(amalgam.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exploit with no stack target still sacrifices and skips the counter")
    void exploitWithNoStackTargetSacrificesOnly() {
        castAmalgamToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Overcharged Amalgam"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Overcharged Amalgam");
        harness.assertInGraveyard(player1, "Overcharged Amalgam");
    }

    @Test
    @DisplayName("Exploit counters a triggered ability that has no targets")
    void exploitCountersUntargetedTriggeredAbility() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new OverchargedAmalgam()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        UUID exploitId = gd.stack.getLast().getTargetableId();

        harness.setHand(player1, List.of(new OverchargedAmalgam()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Overcharged Amalgam"));
        harness.handlePermanentChosen(player1, exploitId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Overcharged Amalgam");
        harness.assertNotInGraveyard(player2, "Overcharged Amalgam");
        harness.assertInGraveyard(player1, "Overcharged Amalgam");
    }

    @Test
    @DisplayName("Removing Amalgam before exploit resolves prevents its counter trigger")
    void removedSourceCanSacrificeWithoutCountering() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        LightningBolt originalSpell = new LightningBolt();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(originalSpell, new OverchargedAmalgam(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Overcharged Amalgam"));
        harness.assertNotOnBattlefield(player1, "Overcharged Amalgam");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(originalSpell);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
