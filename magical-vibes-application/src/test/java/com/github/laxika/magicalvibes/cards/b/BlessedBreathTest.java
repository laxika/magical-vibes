package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedBreath.class, WanderingOnes.class, ReachThroughMists.class, YamabushisFlame.class})
class BlessedBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Wandering Ones"));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        Permanent wanderingOnes = findPermanent(player1, "Wandering Ones");
        assertThat(wanderingOnes.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Wandering Ones")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Blessed Breath in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new ReachThroughMists(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        var targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent wanderingOnes = findPermanent(player1, "Wandering Ones");
        assertThat(wanderingOnes.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        harness.assertInHand(player1, "Blessed Breath");
    }

    @Test
    @DisplayName("Protection from red prevents a red spell from targeting the creature")
    void protectionPreventsRedSpellFromTargeting() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        var targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
