package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NivixGuildmage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychoticFury.class, NivixGuildmage.class, GrizzlyBears.class})
class PsychoticFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Grants double strike to a multicolored creature and draws a card")
    void grantsDoubleStrikeAndDrawsCard() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new NivixGuildmage());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, guildmage.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a multicolored creature")
    void rejectsMonocoloredCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PsychoticFury()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("multicolored creature");
    }
}
