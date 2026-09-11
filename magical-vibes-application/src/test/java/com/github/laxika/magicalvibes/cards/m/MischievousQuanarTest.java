package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MischievousQuanar.class, LavaAxe.class, GrizzlyBears.class})
class MischievousQuanarTest extends BaseCardTest {

    @Test
    void activatedAbilityTurnsItselfFaceDown() {
        Permanent quanar = harness.addToBattlefieldAndReturn(player1, new MischievousQuanar());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(quanar.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, quanar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, quanar)).isEqualTo(2);
    }

    @Test
    void turningFaceUpCopiesTargetInstantOrSorcerySpell() {
        Permanent quanar = castFaceDown();
        LavaAxe lavaAxe = new LavaAxe();
        castLavaAxe(lavaAxe);

        turnFaceUp(quanar);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(lavaAxe.getId());
        harness.handlePermanentChosen(player1, lavaAxe.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        harness.assertInGraveyard(player2, "Lava Axe");
    }

    @Test
    void turningFaceUpCannotTargetCreatureSpell() {
        Permanent quanar = castFaceDown();
        GrizzlyBears bears = new GrizzlyBears();
        castGrizzlyBears(bears);

        turnFaceUp(quanar);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(quanar.isFaceDown()).isFalse();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new MischievousQuanar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Mischievous Quanar");
    }

    private void castLavaAxe(LavaAxe lavaAxe) {
        preparePlayerTwoMainPhase();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);
    }

    private void castGrizzlyBears(GrizzlyBears bears) {
        preparePlayerTwoMainPhase();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
    }

    private void preparePlayerTwoMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void turnFaceUp(Permanent quanar) {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(quanar));
    }
}
