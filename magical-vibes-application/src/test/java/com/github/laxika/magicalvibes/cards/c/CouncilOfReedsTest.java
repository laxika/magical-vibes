package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CouncilOfReeds.class, BurstOfStrength.class, GrizzlyBears.class})
class CouncilOfReedsTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate Council of Reeds creatures survive the legend rule")
    void duplicateCouncilCreaturesSurvive() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        harness.addToBattlefield(player1, new CouncilOfReeds());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not exempt duplicate legendary noncreature permanents")
    void doesNotExemptNoncreatures() {
        harness.addToBattlefield(player1, new CouncilOfReeds());
        Permanent first = addLegendaryArtifact(player1);
        Permanent second = addLegendaryArtifact(player1);

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Creates a token copy at the beginning of combat after a noncreature spell")
    void createsTokenCopyAfterNoncreatureSpell() {
        Permanent council = harness.addToBattlefieldAndReturn(player1, new CouncilOfReeds());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, council.getId());
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(findCouncils()).hasSize(2);
        assertThat(findCouncils()).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a token copy after only a creature spell")
    void doesNotCreateTokenAfterCreatureSpell() {
        harness.addToBattlefield(player1, new CouncilOfReeds());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(findCouncils()).hasSize(1);
    }

    private Permanent addLegendaryArtifact(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears artifact = new GrizzlyBears();
        artifact.setType(CardType.ARTIFACT);
        artifact.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return harness.addToBattlefieldAndReturn(player, artifact);
    }

    private List<Permanent> findCouncils() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Council of Reeds"))
                .toList();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
