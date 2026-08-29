package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AquamorphEntity.class)
class AquamorphEntityTest extends BaseCardTest {

    @Test
    void choosesFiveOneAsItEnters() {
        harness.setHand(player1, List.of(new AquamorphEntity()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("5/1", "1/5");
        harness.handleListChoice(player1, "5/1");

        Permanent entity = findPermanent(player1, "Aquamorph Entity");
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(1);
    }

    @Test
    void choosesOneFiveWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new AquamorphEntity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent entity = findPermanent(player1, "Aquamorph Entity");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(entity));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("5/1", "1/5");
        harness.handleListChoice(player1, "1/5");

        assertThat(entity.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(5);
    }
}
