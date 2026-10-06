package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChaliceOfTheVoid;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HisokaMinamoSensei.class, Shock.class, GrizzlyBears.class,
        CounselOfTheSoratami.class, Fireball.class, ChaliceOfTheVoid.class})
class HisokaMinamoSenseiTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when the discarded card has the same mana value")
    void countersSpellWithMatchingManaValue() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new Shock())); // MV 1, same as the targeted Shock
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({CounselOfTheSoratami.class, Fireball.class})
    @DisplayName("Uses the discarded card's mana value when countering an X spell")
    void countersXSpellUsingAnnouncedManaValue() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new CounselOfTheSoratami())); // MV 3
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Fireball fireball = new Fireball();
        harness.setHand(player2, List.of(fireball));
        harness.addMana(player2, ManaColor.RED, 3); // X=2 plus {R}, so MV 3 on the stack

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, fireball.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Fireball");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell resolves when the discarded card has a different mana value")
    void doesNotCounterOnManaValueMismatch() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new GrizzlyBears())); // MV 2, Shock is MV 1
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shockId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts both X symbols in a target spell's mana value")
    void countersDoubleXSpellWithMatchingManaValue() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new HisokaMinamoSensei()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        ChaliceOfTheVoid chalice = new ChaliceOfTheVoid();
        harness.setHand(player2, List.of(chalice));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player2, 0, 2);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, chalice.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hisoka, Minamo Sensei");
        harness.assertInGraveyard(player2, "Chalice of the Void");
        harness.assertNotOnBattlefield(player2, "Chalice of the Void");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not confuse the chosen X with a double-X spell's mana value")
    void doesNotCounterDoubleXSpellWhenOnlyXMatches() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        ChaliceOfTheVoid chalice = new ChaliceOfTheVoid();
        harness.setHand(player2, List.of(chalice));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castArtifact(player2, 0, 2);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, chalice.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Chalice of the Void");
        harness.assertNotInGraveyard(player2, "Chalice of the Void");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Treats X as zero in the discarded card's mana cost")
    void discardedXCardUsesManaValueOutsideTheStack() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fireball");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's creature spell without tapping")
    void countersOwnCreatureSpellWhileTapped() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        HisokaMinamoSensei spell = new HisokaMinamoSensei();
        harness.setHand(player1, List.of(spell, new HisokaMinamoSensei()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);

        harness.activateAbility(player1, 0, null, spell.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .contains(spell);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
