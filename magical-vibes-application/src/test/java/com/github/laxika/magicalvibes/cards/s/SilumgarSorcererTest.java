package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarSorcerer.class, GrizzlyBears.class, LightningBolt.class})
class SilumgarSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Silumgar Sorcerer on the battlefield")
    void decliningExploitDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SilumgarSorcerer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Silumgar Sorcerer");
    }

    @Test
    @DisplayName("Exploiting another creature counters a creature spell")
    void exploitCountersCreatureSpell() {
        GrizzlyBears sacrifice = new GrizzlyBears();
        harness.addToBattlefield(player2, sacrifice);

        GrizzlyBears target = new GrizzlyBears();
        SilumgarSorcerer sorcerer = new SilumgarSorcerer();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(sorcerer));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Silumgar Sorcerer");
    }

    @Test
    @DisplayName("Exploit does not target a noncreature spell")
    void exploitDoesNotTargetNoncreatureSpell() {
        GrizzlyBears sacrifice = new GrizzlyBears();
        harness.addToBattlefield(player2, sacrifice);

        LightningBolt bolt = new LightningBolt();
        SilumgarSorcerer sorcerer = new SilumgarSorcerer();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(sorcerer));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertOnBattlefield(player2, "Silumgar Sorcerer");
    }
}
