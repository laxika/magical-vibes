package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GnatMiser;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.s.SunderFromWithin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Kuon.class, KuonsEssence.class, GnatMiser.class, KikusShadow.class,
        PithingNeedle.class, SunderFromWithin.class})
class KuonTest extends BaseCardTest {

    @Test
    @DisplayName("Flips at the end step after three creatures die this turn")
    void flipsAfterThreeCreatureDeaths() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GnatMiser());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GnatMiser());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GnatMiser());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new KikusShadow(), new KikusShadow(), new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        for (Permanent target : List.of(first, second, third)) {
            harness.castSorcery(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(kuon.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip at the end step after fewer than three creatures die")
    void doesNotFlipAfterOnlyTwoCreatureDeaths() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GnatMiser());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GnatMiser());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new KikusShadow(), new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (Permanent target : List.of(first, second)) {
            harness.castSorcery(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep();

        assertThat(kuon.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Kuon's Essence makes the active player sacrifice a creature at each upkeep")
    void essenceSacrificesCreatureOfActivePlayerAtUpkeep() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        kuon.setTransformed(true);
        kuon.setCard(kuon.getOriginalCard().getBackFaceCard());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GnatMiser());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GnatMiser());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentArtifact.getId()));
    }

    @Test
    @DisplayName("Does not flip when three noncreature permanents die this turn")
    void doesNotFlipWhenOnlyNoncreaturesDie() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new PithingNeedle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(
                new SunderFromWithin(), new SunderFromWithin(), new SunderFromWithin()));
        harness.addMana(player1, ManaColor.RED, 12);

        for (Permanent target : List.of(first, second, third)) {
            harness.castSorcery(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(kuon.isTransformed()).isFalse();
        harness.assertNotOnBattlefield(player2, "Pithing Needle");
    }

    @Test
    @DisplayName("Kuon's Essence lets the active player choose which creature to sacrifice")
    void essenceLetsActivePlayerChooseCreature() {
        Permanent kuon = harness.addToBattlefieldAndReturn(player1, new Kuon());
        kuon.setTransformed(true);
        kuon.setCard(kuon.getOriginalCard().getBackFaceCard());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GnatMiser());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GnatMiser());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(firstCreature.getId(), secondCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstCreature.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondCreature.getId()));
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
