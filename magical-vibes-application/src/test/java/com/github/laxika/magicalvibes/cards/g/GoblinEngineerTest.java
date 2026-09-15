package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinEngineer.class, Spellbook.class, GrizzlyBears.class, DarksteelIngot.class,
        NevinyrralsDisk.class})
class GoblinEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for an artifact and put it into the graveyard")
    void etbMaySearchForArtifact() {
        castEngineer();
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact, new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Spellbook");

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the ETB search does nothing")
    void decliningEtbSearchDoesNothing() {
        castEngineer();
        Spellbook artifact = new Spellbook();
        harness.setLibrary(player1, List.of(artifact));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInGraveyard(player1, "Spellbook");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("The activated ability sacrifices an artifact and returns an eligible artifact")
    void activationSacrificesArtifactAndReturnsTarget() {
        Permanent engineer = addReadyEngineer();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        DarksteelIngot target = new DarksteelIngot();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(engineer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Spellbook");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotInGraveyard(player1, "Darksteel Ingot");
    }

    @Test
    @DisplayName("The activated ability cannot target a non-artifact or an artifact above mana value 3")
    void activationRejectsIllegalTargets() {
        Permanent engineer = addReadyEngineer();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card nonArtifact = new GrizzlyBears();
        Card expensiveArtifact = new NevinyrralsDisk();
        harness.setGraveyard(player1, List.of(nonArtifact, expensiveArtifact));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, expensiveArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(engineer.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonArtifact, expensiveArtifact);
    }

    private void castEngineer() {
        harness.setHand(player1, List.of(new GoblinEngineer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyEngineer() {
        return addCreatureReady(player1, new GoblinEngineer());
    }
}
