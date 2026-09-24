package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManTitanOfInnovation.class, GoldenEgg.class, SolRing.class, WornPowerstone.class, MindStone.class, DarksteelIngot.class, IronMyr.class})
class IronManTitanOfInnovationTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure and can sacrifice an artifact to find one with one higher mana value")
    void attacksCreatesTreasureAndFindsArtifact() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        DarksteelIngot foundArtifact = new DarksteelIngot();
        harness.setLibrary(player1, List.of(foundArtifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mindStone.getId());

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(foundArtifact);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Darksteel Ingot").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mind Stone");
    }

    @Test
    @DisplayName("Declining the sacrifice still leaves the created Treasure")
    void decliningSacrificeStillCreatesTreasure() {
        addCreatureReady(player1, new IronManTitanOfInnovation());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An artifact creature cannot be sacrificed for the ability")
    void cannotSacrificeArtifactCreature() {
        addCreatureReady(player1, new IronManTitanOfInnovation());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent ironMyr = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent treasure = findPermanent(player1, "Treasure");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(mindStone.getId(), treasure.getId())
                .doesNotContain(ironMyr.getId());
        harness.handlePermanentChosen(player1, mindStone.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ironMyr);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
