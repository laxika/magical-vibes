package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CrypticCoat;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.n.NeurokTransmuter;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.cards.r.RedHerring;
import com.github.laxika.magicalvibes.cards.t.TunnelTipster;
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
        OmegaMyr.class,
        CrypticCoat.class,
        NeurokTransmuter.class,
        RedHerring.class,
        TunnelTipster.class
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
        harness.castFromHand(player1, new UrgentNecropsy(), "{2}{B}{G}");
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

    @Test
    void cloakedTargetRequiresZeroEvidenceRegardlessOfUnderlyingManaCost() {
        harness.setLibrary(player1, List.of(new TunnelTipster()));
        harness.castFromHand(player1, new CrypticCoat(), "{2}{U}");
        resolveAllTriggers();
        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isCloaked).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        harness.castInstant(player1, 0, List.of(cloaked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cloaked);
        harness.assertInGraveyard(player1, "Tunnel Tipster");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void artifactTargetThatLosesArtifactTypeIsNotDestroyedAsACreature() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TunnelTipster());
        List<Card> evidence = List.of(new TunnelTipster(), new TunnelTipster());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();
        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(artifact.getId(), creature.getId()), List.of(), false,
                null, null, null, null, List.of(0, 1));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, artifact)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    @Test
    void canAssignDifferentArtifactCreaturesToArtifactAndCreatureTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RedHerring());
        List<Card> evidence = List.of(new TunnelTipster(), new TunnelTipster());
        harness.setGraveyard(player1, evidence);
        harness.setHand(player1, List.of(new UrgentNecropsy()));
        addManaForUrgentNecropsy();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(first.getId(), second.getId()), List.of(), false,
                null, null, null, null, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
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
