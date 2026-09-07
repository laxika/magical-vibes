package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        UrgentNecropsy.class,
        FountainOfYouth.class,
        GrizzlyBears.class,
        HeartbeatOfSpring.class,
        NicolBolasPlaneswalker.class,
        OmegaMyr.class
})
class UrgentNecropsyTest extends BaseCardTest {

    @Test
    void destroysOneArtifactCreatureEnchantmentAndPlaneswalkerAndCollectsTheirEvidenceValue() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new HeartbeatOfSpring());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        List<Card> evidence = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            evidence.add(new GrizzlyBears());
        }

        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(artifact.getId(), creature.getId(), enchantment.getId(), planeswalker.getId()),
                List.of(), false, null, null, null, null, evidenceIndices(evidence.size()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Heartbeat of Spring");
        harness.assertNotOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void allowsTheSameArtifactCreatureToFillBothTargetCategories() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new OmegaMyr());
        List<Card> evidence = List.of(new GrizzlyBears());

        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(artifactCreature.getId(), artifactCreature.getId()), List.of(), false,
                null, null, null, null, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Omega Myr");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(evidence);
    }

    @Test
    void rejectsInsufficientEvidenceForTargetManaValues() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must collect evidence");
    }

    @Test
    void mayChooseNoTargetsAndCollectNoEvidence() {
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void rejectsMoreThanOneCreatureTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature");
    }

    private void addManaForUrgentNecropsy() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Integer> evidenceIndices(int count) {
        return IntStream.range(0, count).boxed().toList();
    }
}
