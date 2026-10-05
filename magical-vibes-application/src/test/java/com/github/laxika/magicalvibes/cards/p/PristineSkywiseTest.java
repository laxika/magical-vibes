package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({PristineSkywise.class, Shock.class, GrizzlyBears.class})
class PristineSkywiseTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell untaps Pristine Skywise and grants chosen-color protection")
    void noncreatureSpellUntapsAndGrantsProtection() {
        Permanent skywise = addCreatureReady(player1, new PristineSkywise());
        skywise.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(skywise.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gqs.hasProtectionFrom(gd, skywise, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, skywise, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Pristine Skywise")
    void creatureSpellDoesNotTrigger() {
        Permanent skywise = addCreatureReady(player1, new PristineSkywise());
        skywise.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(skywise.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Pristine Skywise")
    void opponentNoncreatureSpellDoesNotTrigger() {
        Permanent skywise = addCreatureReady(player1, new PristineSkywise());
        skywise.tap();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(skywise.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Protection chosen before the triggering spell resolves makes its target illegal")
    void protectionMakesTriggeringSpellTargetIllegal() {
        Permanent skywise = addCreatureReady(player1, new PristineSkywise());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, skywise.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(gqs.hasProtectionFrom(gd, skywise, CardColor.RED)).isTrue();
        assertThat(skywise.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Repeated triggers add protection colors and affect only their source")
    void repeatedTriggersAccumulateProtectionOnlyOnSource() {
        Permanent skywise = addCreatureReady(player1, new PristineSkywise());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.tap();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();
        skywise.tap();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "WHITE");
        resolveAllTriggers();

        assertThat(skywise.isTapped()).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, skywise, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, skywise, CardColor.WHITE)).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, other, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, other, CardColor.WHITE)).isFalse();
    }
}
