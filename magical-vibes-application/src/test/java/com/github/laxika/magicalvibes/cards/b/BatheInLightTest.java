package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatheInLight.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class BatheInLightTest extends BaseCardTest {

    @Test
    @DisplayName("Protects the target and every creature sharing a color with it")
    void protectsTargetAndColorSharingCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownMatchingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentMatchingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(ownMatchingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(opponentMatchingCreature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(differentColorCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("A colorless target does not share a color with other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        assertThat(otherColorlessCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
        assertThat(coloredCreature.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Fizzles without a color choice if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BatheInLight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }
}
