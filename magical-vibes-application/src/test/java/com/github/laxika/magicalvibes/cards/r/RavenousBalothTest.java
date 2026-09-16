package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.cards.k.KrosanGroundshaker;
import com.github.laxika.magicalvibes.cards.m.MorcantsEyes;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousBaloth.class, KrosanGroundshaker.class, GoblinSledder.class})
class RavenousBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself as a Beast gains 4 life")
    void sacrificeItselfGainsFourLife() {
        harness.addToBattlefield(player1, new RavenousBaloth());
        harness.setLife(player1, 10);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Ravenous Baloth");
    }

    @Test
    @DisplayName("Can sacrifice another Beast and gains 4 life")
    void sacrificeAnotherBeastGainsFourLife() {
        harness.addToBattlefield(player1, new RavenousBaloth());
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new KrosanGroundshaker());
        harness.setLife(player1, 10);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, beast.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Krosan Groundshaker");
        harness.assertOnBattlefield(player1, "Ravenous Baloth");
    }

    @Test
    @DisplayName("Does not sacrifice a non-Beast creature")
    void doesNotSacrificeNonBeastCreature() {
        harness.addToBattlefield(player1, new RavenousBaloth());
        harness.addToBattlefield(player1, new GoblinSledder());
        harness.setLife(player1, 10);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Ravenous Baloth");
        harness.assertOnBattlefield(player1, "Goblin Sledder");
    }

    @Test
    @CardUsed(MorcantsEyes.class)
    @DisplayName("Can sacrifice a noncreature Kindred permanent with the Beast type")
    void canSacrificeKindredBeastPermanent() {
        harness.addToBattlefield(player1, new RavenousBaloth());
        Permanent kindredBeast = harness.addToBattlefieldAndReturn(player1, new MorcantsEyes());
        TestCards.mutableCard(kindredBeast).setSubtypes(List.of(CardSubtype.BEAST));
        harness.setLife(player1, 10);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, kindredBeast.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInGraveyard(player1, "Morcant's Eyes");
        harness.assertOnBattlefield(player1, "Ravenous Baloth");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
