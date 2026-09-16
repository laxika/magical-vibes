package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.SecludedSteppe;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromasBlessing.class, GlorySeeker.class, SecludedSteppe.class, Shock.class})
class AkromasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures you control gain protection from the chosen color until end of turn")
    void grantsProtectionToOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new SecludedSteppe());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.castFromHand(player1, new AkromasBlessing(), "{2}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(ownCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownOtherCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownLand.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(opposingCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Protection prevents a spell of the chosen color from targeting those creatures")
    void protectionStopsRedSpellTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.castFromHand(player1, new AkromasBlessing(), "{2}{W}");

        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.castFromHand(player1, new AkromasBlessing(), "{2}{W}");

        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Cycling {W} discards Akroma's Blessing and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AkromasBlessing()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Akroma's Blessing");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
