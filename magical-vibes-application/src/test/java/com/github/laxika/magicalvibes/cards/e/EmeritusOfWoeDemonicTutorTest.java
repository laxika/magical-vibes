package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmeritusOfWoeDemonicTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters prepared with a Demonic Tutor copy")
    void entersPrepared() {
        Permanent emeritus = castEmeritus();

        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(emeritus.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting Demonic Tutor unprepares Emeritus and searches the library")
    void castingPreparedTutorUnpreparesAndSearches() {
        Permanent emeritus = castEmeritus();
        castPreparedTutor(emeritus);

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Reprepares at your end step after two creatures die across the players")
    void repreparesAfterTwoCreaturesDie() {
        Permanent emeritus = castEmeritus();
        castPreparedTutor(emeritus);

        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);
        advanceToEndStepAndResolve();

        assertThat(emeritus.isPrepared()).isTrue();
    }

    @Test
    @DisplayName("Does not reprepare at your end step after only one creature dies")
    void doesNotReprepareAfterOneCreatureDies() {
        Permanent emeritus = castEmeritus();
        castPreparedTutor(emeritus);

        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);
        advanceToEndStepAndResolve();

        assertThat(emeritus.isPrepared()).isFalse();
    }

    private Permanent castEmeritus() {
        harness.setHand(player1, List.of(new EmeritusOfWoeDemonicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EmeritusOfWoeDemonicTutor)
                .findFirst()
                .orElseThrow();
    }

    private void castPreparedTutor(Permanent emeritus) {
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void advanceToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
