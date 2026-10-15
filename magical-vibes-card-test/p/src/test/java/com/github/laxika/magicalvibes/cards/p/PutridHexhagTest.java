package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridHexhag.class, BurstOfStrength.class, Murder.class, GrizzlyBears.class, Unsummon.class})
class PutridHexhagTest extends BaseCardTest {

    @Test
    @DisplayName("A counter put on Putrid Hexhag lets it perpetually curse an opponent creature")
    void cursesOpponentCreatureAndItsControllerLosesLifeWhenItDies() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(hexhag);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        destroy(player1, target);
        int lifeBeforeTrigger = gd.getLife(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeTrigger - 2);
    }

    @Test
    @DisplayName("The counter trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        putCounterOn(hexhag);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Separate counter events grant independent copies of the death ability")
    void repeatedCursesCauseLifeLossForEachGrantedAbility() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        for (int i = 0; i < 2; i++) {
            putCounterOn(hexhag);
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
        }

        int lifeBefore = gd.getLife(player2.getId());
        destroy(player1, target);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("The curse remains when the creature returns to hand and is cast again")
    void curseSurvivesReturningToHandAndRecasting() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        putCounterOn(hexhag);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        destroy(player1, findPermanent(player2, "Grizzly Bears"));
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The counter trigger still grants the curse after Putrid Hexhag dies")
    void triggerResolvesAfterHexhagLeavesBattlefield() {
        Permanent hexhag = addCreatureReady(player1, new PutridHexhag());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        putCounterOn(hexhag);
        harness.handlePermanentChosen(player1, target.getId());

        destroy(player1, hexhag);
        harness.passBothPriorities();
        destroy(player1, target);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void putCounterOn(Permanent hexhag) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hexhag.getId());
    }

    private void destroy(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
