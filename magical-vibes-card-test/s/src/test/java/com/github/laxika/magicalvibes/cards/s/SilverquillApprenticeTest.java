package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.EssenceInfusion;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({SilverquillApprentice.class, BarkshellBlessing.class, EssenceInfusion.class, GiantGrowth.class,
        GrizzlyBears.class, HillGiant.class, Shock.class})
class SilverquillApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts a chosen creature until end of turn")
    void castingInstantBoostsChosenCreature() {
        addCreatureReady(player1, new SilverquillApprentice());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copying an instant triggers Silverquill Apprentice")
    void copyingInstantBoostsChosenCreatureAgain() {
        addCreatureReady(player1, new SilverquillApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SilverquillApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void castingCreatureDoesNotTriggerMagecraft() {
        addCreatureReady(player1, new SilverquillApprentice());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @CardUsed({SilverquillApprentice.class, EssenceInfusion.class})
    @DisplayName("Casting a sorcery can boost a creature other than the spell's target")
    void castingSorceryBoostsIndependentTarget() {
        Permanent apprentice = addCreatureReady(player1, new SilverquillApprentice());
        Permanent spellTarget = addCreatureReady(player2, new SilverquillApprentice());
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, spellTarget.getId());
        harness.handlePermanentChosen(player1, apprentice.getId());
        harness.passBothPriorities();

        assertThat(apprentice.getEffectivePower()).isEqualTo(3);
        assertThat(apprentice.getEffectiveToughness()).isEqualTo(2);
        assertThat(spellTarget.getEffectivePower()).isEqualTo(2);
        assertThat(spellTarget.getEffectiveToughness()).isEqualTo(2);

        resolveAllTriggers();

        assertThat(apprentice.getEffectivePower()).isEqualTo(3);
        assertThat(spellTarget.getEffectivePower()).isEqualTo(4);
        assertThat(spellTarget.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @CardUsed({SilverquillApprentice.class, EssenceInfusion.class})
    @DisplayName("An opponent's sorcery does not trigger magecraft")
    void opponentCastingSorceryDoesNotTriggerMagecraft() {
        Permanent apprentice = addCreatureReady(player1, new SilverquillApprentice());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new EssenceInfusion()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, apprentice.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(apprentice.getEffectivePower()).isEqualTo(4);
        assertThat(apprentice.getEffectiveToughness()).isEqualTo(4);
    }
}
