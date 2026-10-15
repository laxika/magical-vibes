package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LunarForce.class, GrizzlyBears.class, Forest.class, Opt.class})
class LunarForceTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's spell causes Lunar Force to be sacrificed and countered")
    void sacrificesAndCountersOpponentsSpell() {
        harness.addToBattlefield(player1, new LunarForce());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lunar Force");
        harness.assertInGraveyard(player1, "Lunar Force");
        harness.assertInGraveyard(player2, "Opt");
        harness.assertNotOnBattlefield(player2, "Opt");
    }

    @Test
    @DisplayName("Lunar Force does not trigger for its controller's spell")
    void doesNotTriggerForControllersSpell() {
        harness.addToBattlefield(player1, new LunarForce());

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lunar Force");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Playing a land does not trigger Lunar Force")
    void playingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new LunarForce());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lunar Force");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Both spells are countered when an opponent responds before Lunar Force is sacrificed")
    void countersBothSpellsWithOnlyOneSacrifice() {
        harness.addToBattlefield(player1, new LunarForce());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Opt(), new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0);
        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(4);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lunar Force");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature spell is countered before entering the battlefield")
    void countersOpponentsCreatureSpell() {
        harness.addToBattlefield(player1, new LunarForce());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lunar Force");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Lunar Force is sacrificed even if another trigger already countered the spell")
    void sacrificesBothEnchantmentsForOneSpell() {
        harness.addToBattlefield(player1, new LunarForce());
        harness.addToBattlefield(player1, new LunarForce());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Lunar Force");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Opt");
    }
}
