package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.d.DaxosOfMeletis;
import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NykthosShrineToNyx.class, BowOfNylea.class, VoyagingSatyr.class, DaxosOfMeletis.class})
class NykthosShrineToNyxTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new NykthosShrineToNyx());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The second ability adds mana equal to the chosen color's devotion")
    void addsManaEqualToChosenColorDevotion() {
        harness.addToBattlefield(player1, new NykthosShrineToNyx());
        harness.addToBattlefield(player1, new BowOfNylea());
        harness.addToBattlefield(player1, new VoyagingSatyr());
        harness.addToBattlefield(player2, new BowOfNylea());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Choosing a color with no devotion produces no mana")
    void noDevotionProducesNoMana() {
        harness.addToBattlefield(player1, new NykthosShrineToNyx());
        harness.addToBattlefield(player1, new BowOfNylea());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void devotionAbilityPaysTwoManaAndTapsWithoutUsingTheStack() {
        var nykthos = harness.addToBattlefieldAndReturn(player1, new NykthosShrineToNyx());
        harness.addToBattlefield(player1, new BowOfNylea());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(nykthos.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseProducedManaToPayItsOwnActivationCost() {
        var nykthos = harness.addToBattlefieldAndReturn(player1, new NykthosShrineToNyx());
        harness.addToBattlefield(player1, new BowOfNylea());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(nykthos.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    void multicoloredPermanentContributesOnlySymbolsOfTheChosenColor(ManaColor color) {
        harness.addToBattlefield(player1, new NykthosShrineToNyx());
        harness.addToBattlefield(player1, new DaxosOfMeletis());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    void tappedNoncreaturePermanentsCountButCardsOutsideBattlefieldDoNot() {
        harness.addToBattlefield(player1, new NykthosShrineToNyx());
        harness.addToBattlefieldAndReturn(player1, new BowOfNylea()).setTapped(true);
        harness.setHand(player1, List.of(new VoyagingSatyr()));
        harness.setGraveyard(player1, List.of(new VoyagingSatyr()));
        harness.setExile(player1, List.of(new VoyagingSatyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
