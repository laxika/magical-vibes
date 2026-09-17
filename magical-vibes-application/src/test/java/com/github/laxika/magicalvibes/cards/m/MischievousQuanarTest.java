package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Carbonize;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.g.GoblinWarStrike;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MischievousQuanar.class, Carbonize.class, GoblinBrigand.class,
        GoblinWarStrike.class, ScornfulEgotist.class})
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
        Carbonize carbonize = new Carbonize();
        castCarbonize(carbonize);

        turnFaceUp(quanar);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(carbonize.getId());
        harness.handlePermanentChosen(player1, carbonize.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInGraveyard(player2, "Carbonize");
    }

    @Test
    void turningFaceUpCopiesTargetSorcerySpell() {
        Permanent quanar = castFaceDown();
        addCreatureReady(player1, new GoblinBrigand());
        addCreatureReady(player2, new GoblinBrigand());
        GoblinWarStrike warStrike = new GoblinWarStrike();
        castGoblinWarStrike(warStrike);

        turnFaceUp(quanar);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(warStrike.getId());
        harness.handlePermanentChosen(player1, warStrike.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Goblin War Strike");
    }

    @Test
    void turningFaceUpMayRetargetCopiedSpell() {
        Permanent quanar = castFaceDown();
        Carbonize carbonize = new Carbonize();
        castCarbonize(carbonize);

        turnFaceUp(quanar);
        harness.handlePermanentChosen(player1, carbonize.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void turningFaceUpCannotTargetCreatureSpell() {
        Permanent quanar = castFaceDown();
        ScornfulEgotist egotist = new ScornfulEgotist();
        castScornfulEgotist(egotist);

        turnFaceUp(quanar);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(quanar.isFaceDown()).isFalse();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Scornful Egotist");
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

    private void castCarbonize(Carbonize carbonize) {
        preparePlayerTwoMainPhase();
        harness.setHand(player2, List.of(carbonize));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
    }

    private void castGoblinWarStrike(GoblinWarStrike warStrike) {
        preparePlayerTwoMainPhase();
        harness.setHand(player2, List.of(warStrike));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);
    }

    private void castScornfulEgotist(ScornfulEgotist egotist) {
        preparePlayerTwoMainPhase();
        harness.castFromHand(player2, egotist, "{7}{U}");
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
