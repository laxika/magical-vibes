package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeritageReclamation.class, AirElemental.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class})
class HeritageReclamationTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Test
    void destroysTargetArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new HeritageReclamation()));
        addMana();

        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void destroysTargetEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        harness.setHand(player1, List.of(new HeritageReclamation()));
        addMana();

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void rejectsWrongPermanentTypeForSelectedMode() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();
        harness.setHand(player1, List.of(new HeritageReclamation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesUpToOneGraveyardCardAndDraws() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void drawsWithNoTargetWhenGraveyardsAreEmpty() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Heritage Reclamation");
    }

    @Test
    void mayDeclineGraveyardTargetEvenWhenOneIsAvailable() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        addMana();

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    void canExileNoncreatureCardFromOwnGraveyard() {
        Card artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0, 2, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void doesNotDrawWhenChosenGraveyardTargetBecomesIllegal() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player1, List.of(new HeritageReclamation()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        addMana();

        harness.castInstant(player1, 0, 2, bears.getId());
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(bears));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Heritage Reclamation");
    }

    @Test
    void enchantmentModeRejectsArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new HeritageReclamation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactModeRejectsEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        harness.setHand(player1, List.of(new HeritageReclamation()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
