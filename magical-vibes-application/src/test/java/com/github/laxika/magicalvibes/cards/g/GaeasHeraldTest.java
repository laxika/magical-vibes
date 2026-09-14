package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasHerald.class, GrizzlyBears.class, Cancel.class, MightOfOaks.class, SpiketailHatchling.class})
class GaeasHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells are not countered while Gaea's Herald is on battlefield")
    void protectsCreatureSpellsFromCounterspells() {
        harness.addToBattlefield(player1, new GaeasHerald());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creature spells controlled by either player are not countered")
    void protectsOpponentsCreatureSpellsFromCounterspells() {
        harness.addToBattlefield(player1, new GaeasHerald());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    @DisplayName("Noncreature spells can still be countered while Gaea's Herald is on battlefield")
    void doesNotProtectNonCreatureSpells() {
        harness.addToBattlefield(player1, new GaeasHerald());
        harness.addToBattlefield(player1, new GrizzlyBears());

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Might of Oaks");
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Might of Oaks"));
    }

    @Test
    @DisplayName("Creature spells are not countered by counter-unless-pays abilities")
    void protectsCreatureSpellsFromCounterUnlessPaysAbilities() {
        harness.addToBattlefield(player1, new GaeasHerald());
        harness.addToBattlefield(player2, new SpiketailHatchling());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gaea's Herald can be countered before it enters the battlefield")
    void canBeCounteredBeforeEnteringTheBattlefield() {
        GaeasHerald herald = new GaeasHerald();
        harness.setHand(player1, List.of(herald));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, herald.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gaea's Herald");
        harness.assertNotOnBattlefield(player1, "Gaea's Herald");
    }
}
