package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KherKeep.class})
class KherKeepTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless")
    void tapsForColorless() {
        harness.addToBattlefield(player1, new KherKeep());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability creates a named 0/1 red Kobold")
    void createsKoboldToken() {
        harness.addToBattlefield(player1, new KherKeep());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent kobold = findPermanent(player1, "Kobolds of Kher Keep");
        assertThat(kobold.getCard().isToken()).isTrue();
        assertThat(gqs.isCreature(gd, kobold)).isTrue();
        assertThat(gqs.getEffectivePower(gd, kobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, kobold)).containsExactly(CardColor.RED);
        assertThat(kobold.getCard().getSubtypes()).containsExactly(CardSubtype.KOBOLD);
    }

    @Test
    @DisplayName("Token ability requires one generic and one red mana")
    void requiresManaForTokenAbility() {
        harness.addToBattlefield(player1, new KherKeep());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana ability taps the land and resolves without using the stack")
    void manaAbilityTapsAndResolvesImmediately() {
        Permanent keep = harness.addToBattlefieldAndReturn(player1, new KherKeep());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(keep.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the red token activation cost")
    void tokenAbilityRequiresRedMana() {
        Permanent keep = harness.addToBattlefieldAndReturn(player1, new KherKeep());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(keep.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Kobolds of Kher Keep")).isZero();
    }

    @Test
    @DisplayName("Token activation taps and spends mana before creating one untapped token for its controller")
    void tokenAbilityUsesStackAndCreatesTokenForItsController() {
        Permanent keep = harness.addToBattlefieldAndReturn(player2, new KherKeep());
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(keep.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player2, "Kobolds of Kher Keep")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Kobolds of Kher Keep")).isEqualTo(1);
        assertThat(countPermanents(player1, "Kobolds of Kher Keep")).isZero();
        assertThat(findPermanent(player2, "Kobolds of Kher Keep").isTapped()).isFalse();
        harness.addMana(player2, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Kobolds of Kher Keep")).isEqualTo(1);
    }
}
