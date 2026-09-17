package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartbeatOfSpring;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.o.OmegaMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ChaoticTransformation.class,
        Forest.class,
        FountainOfYouth.class,
        GrizzlyBears.class,
        HeartbeatOfSpring.class,
        MoxOpal.class,
        NicolBolasPlaneswalker.class,
        OmegaMyr.class
})
class ChaoticTransformationTest extends BaseCardTest {

    @Test
    void exilesEachTargetAndReplacesItWithApermanentSharingItsCardType() {
        Card artifactCard = new FountainOfYouth();
        Card creatureCard = new GrizzlyBears();
        Card enchantmentCard = new HeartbeatOfSpring();
        Card planeswalkerCard = new NicolBolasPlaneswalker();
        Card landCard = new Forest();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, artifactCard);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, enchantmentCard);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent land = harness.addToBattlefieldAndReturn(player2, landCard);

        harness.setLibrary(player2, List.of(
                new Forest(), new HeartbeatOfSpring(),
                new NicolBolasPlaneswalker(), new MoxOpal(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castSorcery(player1, 0,
                List.of(artifact.getId(), creature.getId(), enchantment.getId(), planeswalker.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(artifactCard, creatureCard, enchantmentCard, planeswalkerCard, landCard);
        assertThat(countPermanents(player2, "Mox Opal")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Heartbeat of Spring")).isEqualTo(1);
        assertThat(countPermanents(player2, "Nicol Bolas, Planeswalker")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
    }

    @Test
    void allowsAnArtifactCreatureToFillBothCategoriesButReplacesItOnce() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new OmegaMyr());
        Card creatureLeftInLibrary = new GrizzlyBears();
        harness.setLibrary(player2, List.of(new MoxOpal(), creatureLeftInLibrary));
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castSorcery(player1, 0, List.of(artifactCreature.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Omega Myr");
        assertThat(countPermanents(player2, "Mox Opal")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creatureLeftInLibrary);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifactCreature.getCard());
    }

    @Test
    void rejectsMoreThanOneTargetForTheSameCardType() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one artifact");
    }

    @Test
    void mayBeCastWithNoTargets() {
        harness.setHand(player1, List.of(new ChaoticTransformation()));
        addManaForChaoticTransformation();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void addManaForChaoticTransformation() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
