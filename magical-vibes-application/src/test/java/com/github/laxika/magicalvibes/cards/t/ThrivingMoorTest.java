package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ThrivingMoor.class)
class ThrivingMoorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and offers every color except black")
    void entersTappedAndChoosesNonBlackColor() {
        harness.setHand(player1, List.of(new ThrivingMoor()));

        harness.playLand(player1, 0);

        Permanent moor = findPermanent(player1, "Thriving Moor");
        assertThat(moor.isTapped()).isTrue();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "RED", "GREEN");
        assertThat(choice.options()).doesNotContain("BLACK");

        harness.handleListChoice(player1, "GREEN");

        assertThat(moor.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("The first mana ability adds black mana")
    void firstAbilityAddsBlackMana() {
        Permanent moor = addReadyMoor();
        moor.setChosenColor(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(moor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second mana ability adds mana of the chosen color")
    void secondAbilityAddsChosenColorMana() {
        Permanent moor = addReadyMoor();
        moor.setChosenColor(CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(moor.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = CardColor.class, names = {"WHITE", "BLUE", "RED", "GREEN"})
    @DisplayName("Each allowed entry choice produces exactly one mana without using the stack")
    void entryChoiceDeterminesManaProduced(CardColor color) {
        harness.setHand(player1, List.of(new ThrivingMoor()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        Permanent moor = findPermanent(player1, "Thriving Moor");
        assertThat(moor.getChosenColor()).isEqualTo(color);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        moor.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor.name().equals(color.name()) ? 1 : 0);
        }
        assertThat(moor.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void enteringTappedPreventsManaActivation(int abilityIndex) {
        harness.setHand(player1, List.of(new ThrivingMoor()));
        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(findPermanent(player1, "Thriving Moor").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Black is rejected as the entry color and the legal choice remains pending")
    void rejectsBlackEntryChoice() {
        harness.setHand(player1, List.of(new ThrivingMoor()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(findPermanent(player1, "Thriving Moor").getChosenColor()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "WHITE");
        assertThat(findPermanent(player1, "Thriving Moor").getChosenColor()).isEqualTo(CardColor.WHITE);
    }

    private Permanent addReadyMoor() {
        Permanent moor = harness.addToBattlefieldAndReturn(player1, new ThrivingMoor());
        moor.setSummoningSick(false);
        return moor;
    }
}
