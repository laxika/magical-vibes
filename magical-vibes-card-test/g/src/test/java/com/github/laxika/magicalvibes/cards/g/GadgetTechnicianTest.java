package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GadgetTechnician.class})
class GadgetTechnicianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void entersAndCreatesThopter() {
        harness.setHand(player1, List.of(new GadgetTechnician()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThopterCreated();
    }

    @Test
    @DisplayName("Turning face up creates a 1/1 colorless Thopter artifact creature token with flying")
    void turnsFaceUpAndCreatesThopter() {
        harness.setHand(player1, List.of(new GadgetTechnician()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent technician = findPermanent(player1, "Gadget Technician");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(technician));
        harness.passBothPriorities();

        assertThat(technician.isFaceDown()).isFalse();
        assertThopterCreated();
    }

    @Test
    @DisplayName("Entering face down does not create a Thopter")
    void faceDownEntryDoesNotCreateThopter() {
        harness.setHand(player1, List.of(new GadgetTechnician()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent technician = findPermanent(player1, "Gadget Technician");
        assertThat(technician.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, technician)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, technician)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, technician, Keyword.WARD)).isTrue();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "RED"})
    @DisplayName("Either two blue or two red mana pays the disguise cost")
    void turnsFaceUpWithSameColorHybridPayment(ManaColor color) {
        harness.setHand(player1, List.of(new GadgetTechnician()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent technician = findPermanent(player1, "Gadget Technician");
        harness.addMana(player1, color, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(technician));
        harness.passBothPriorities();

        assertThat(technician.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, technician, Keyword.WARD)).isFalse();
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThopterCreated();
    }

    private void assertThopterCreated() {
        Permanent thopter = findPermanents(player1, "Thopter").stream().findFirst().orElse(null);

        assertThat(thopter).isNotNull();
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }
}
