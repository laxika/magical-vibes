package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfTheGuildpact.class, SeleniaDarkAngel.class})
class SealOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a spell by one generic mana for each matching chosen color")
    void reducesForEachMatchingChosenColor() {
        Permanent seal = addSealWithChosenColors(CardColor.WHITE, CardColor.BLACK);
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(seal.getChosenColors()).containsExactly(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("Reduces by only the number of chosen colors present in the spell")
    void reducesByOnlyMatchingChosenColors() {
        addSealWithChosenColors(CardColor.WHITE, CardColor.GREEN);
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce a spell with neither chosen color")
    void doesNotReduceWithoutMatchingChosenColor() {
        addSealWithChosenColors(CardColor.BLUE, CardColor.GREEN);
        harness.setHand(player1, List.of(new SeleniaDarkAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSealWithChosenColors(CardColor first, CardColor second) {
        harness.addToBattlefield(player1, new SealOfTheGuildpact());
        Permanent seal = findPermanent(player1, "Seal of the Guildpact");
        seal.getChosenColors().addAll(List.of(first, second));
        return seal;
    }
}
