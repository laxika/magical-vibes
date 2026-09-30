package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorElixir;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CityOfTheDaleks.class, GrizzlyBears.class, IchorElixir.class, PalladiumMyr.class})
class CityOfTheDaleksTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new CityOfTheDaleks(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void attackingMakesTargetOpponentLoseLifeEqualToYourArtifacts() {
        harness.addToBattlefield(player1, new IchorElixir());
        harness.addToBattlefield(player1, new PalladiumMyr());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IchorElixir());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(player1.getId(), opponentArtifact.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void chaosCreatesHastyMenacingArtifactDaleksThatMustAttackOpponentAndSacrifice() {
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        Permanent dalek = findPermanent(player1, "Dalek");
        assertThat(dalek.getCard().isToken()).isTrue();
        assertThat(dalek.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalek, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dalek, Keyword.HASTE)).isTrue();
        assertThat(dalek.isTapped()).isFalse();
        assertThat(dalek.isMustAttackThisTurn()).isTrue();
        assertThat(dalek.getMustAttackTargetId()).isEqualTo(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dalek")).isEmpty();
    }
}
