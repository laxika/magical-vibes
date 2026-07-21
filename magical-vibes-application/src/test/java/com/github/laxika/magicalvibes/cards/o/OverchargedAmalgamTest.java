package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Overcharged Amalgam"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Overcharged Amalgam"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Overcharged Amalgam"));
    }

    @Test
    @DisplayName("Exploit can counter an activated ability")
    void exploitCountersActivatedAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();
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
        Permanent amalgam = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Overcharged Amalgam"))
                .findFirst().orElseThrow();
        assertThat(amalgam.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exploit with no stack target still sacrifices and skips the counter")
    void exploitWithNoStackTargetSacrificesOnly() {
        castAmalgamToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Overcharged Amalgam"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Overcharged Amalgam"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Overcharged Amalgam"));
    }
}
