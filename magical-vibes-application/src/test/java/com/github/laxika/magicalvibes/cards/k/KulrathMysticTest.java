package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KulrathMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater gives +2/+0 and vigilance")
    void highManaValueSpellBoostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Kulrath Mystic");
        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not trigger Kulrath Mystic")
    void lowManaValueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Kulrath Mystic");
        assertThat(mystic.getPowerModifier()).isEqualTo(0);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void boostAndVigilanceWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Kulrath Mystic");
        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
