package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaridEnterprisingSalvager.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class FaridEnterprisingSalvagerTest extends BaseCardTest {

    @Test
    void createsScrapWhenYourNontokenArtifactGoesToYourGraveyard() {
        addReadyFarid();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        putIntoGraveyard(ownArtifact);

        assertThat(findPermanents(player1, "Scrap")).hasSize(1);
        assertThat(findPermanents(player2, "Scrap")).isEmpty();
    }

    @Test
    void counterModePutsACounterOnFaridAndGrantsMenace() {
        Permanent farid = addReadyFarid();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(farid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(farid.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(findPermanents(player1, "Scrap")).hasSize(1);
    }

    @Test
    void goadModeGoadsTargetCreature() {
        addReadyFarid();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    void rummageModeDiscardsThenDraws() {
        addReadyFarid();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Forest discarded = new Forest();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void goadModeRejectsNonCreatureTargets() {
        addReadyFarid();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    private Permanent addReadyFarid() {
        Permanent farid = harness.addToBattlefieldAndReturn(player1, new FaridEnterprisingSalvager());
        farid.setSummoningSick(false);
        return farid;
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }
}
