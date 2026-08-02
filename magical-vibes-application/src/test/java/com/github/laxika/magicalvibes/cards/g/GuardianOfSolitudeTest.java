package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianOfSolitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell grants flying to the chosen creature")
    void arcaneSpellGrantsFlying() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        assertThat(retainer.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a Spirit spell grants flying to the chosen creature")
    void spiritSpellGrantsFlying() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        assertThat(retainer.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        Permanent retainer = addCreatureReady(player1, new DevotedRetainer());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, retainer.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(retainer.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Guardian of Solitude")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuardianOfSolitude());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
