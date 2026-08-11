package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlloyGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a color as it enters and becomes that color")
    void choosesColorOnEntry() {
        harness.setHand(player1, List.of(new AlloyGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent golem = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(golem.getChosenColors()).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, golem)).containsExactly(CardColor.RED);
    }
}
