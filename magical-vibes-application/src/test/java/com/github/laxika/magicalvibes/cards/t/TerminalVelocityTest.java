package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TerminalVelocity.class, AirElemental.class, GrizzlyBears.class, Mountain.class, Ornithopter.class,
        Dominate.class, Unsummon.class, MindStone.class})
class TerminalVelocityTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact and creature cards in hand")
    void offersArtifactsAndCreatures() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new Mountain(), new GrizzlyBears(), new Ornithopter()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("The chosen permanent has haste, deals damage on leaving, and is sacrificed at end step")
    void chosenPermanentGetsAllGrantedAbilities() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new AirElemental()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the hand unchanged")
    void decliningLeavesCardInHand() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new GrizzlyBears()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning the permanent to hand damages creatures on both sides but not players")
    void returningToHandDealsManaValueDamage() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new AirElemental(), new Unsummon()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Air Elemental"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A zero mana value artifact creature deals no damage when sacrificed")
    void zeroManaValueDealsNoDamage() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new Ornithopter()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature artifact can be put onto the battlefield and deals its mana value on leaving")
    void noncreatureArtifactGetsGrantedAbilities() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new MindStone()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Mind Stone");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("The sacrifice ability follows the permanent's controller and persists until their end step")
    void sacrificeFollowsNewController() {
        harness.setHand(player1, List.of(new TerminalVelocity(), new AirElemental()));
        harness.setHand(player2, List.of(new Dominate()));
        harness.setLibrary(player2, List.of(new Mountain()));
        addMana();
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player2, ManaColor.BLUE, 8);
        harness.castInstant(player2, 0, 5, harness.getPermanentId(player1, "Air Elemental"));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Air Elemental");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
