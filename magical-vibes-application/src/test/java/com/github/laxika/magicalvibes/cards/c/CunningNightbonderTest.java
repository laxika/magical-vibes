package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrinebornCutthroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningNightbonder.class, BrinebornCutthroat.class, Cancel.class, GrizzlyBears.class})
class CunningNightbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Flash spells cost {1} less to cast")
    void flashSpellsCostOneLess() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new BrinebornCutthroat()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Spells without flash are not reduced")
    void spellsWithoutFlashAreNotReduced() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash spells you cast cannot be countered")
    void flashSpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        BrinebornCutthroat cutthroat = new BrinebornCutthroat();
        harness.setHand(player1, List.of(cutthroat));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cutthroat.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brineborn Cutthroat");
        harness.assertInGraveyard(player2, "Cancel");
    }
}
