package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DiamondKaleidoscope.class)
class DiamondKaleidoscopeTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T} creates a 0/1 colorless Prism artifact creature token")
    void createsPrismToken() {
        Permanent kaleidoscope = harness.addToBattlefieldAndReturn(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(kaleidoscope.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent prism = findPermanent(player1, "Prism");
        assertThat(prism.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, prism)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, prism)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, prism)).isTrue();
        assertThat(gqs.isCreature(gd, prism)).isTrue();
        assertThat(prism.getCard().getSubtypes()).contains(CardSubtype.PRISM);
        assertThat(gqs.getEffectiveColors(gd, prism)).isEmpty();
    }

    @Test
    @DisplayName("Creating a Prism token pays three generic mana")
    void creatingPrismPaysThreeGenericMana() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a Prism token adds one mana of the chosen color")
    void sacrificePrismAddsAnyColor() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.assertNotOnBattlefield(player1, "Prism");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With multiple Prism tokens, sacrificing one leaves the others on the battlefield")
    void sacrificesOnlyTheChosenPrismToken() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> prisms = findPermanents(player1, "Prism");
        assertThat(prisms).hasSize(2);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                prisms.stream().map(Permanent::getId).toList());

        harness.handlePermanentChosen(player1, prisms.getFirst().getId());
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanents(player1, "Prism")).containsExactly(prisms.get(1));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice for mana with no Prism token")
    void cannotSacrificeWithoutPrism() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot create a Prism without paying all three mana")
    void cannotCreatePrismWithInsufficientMana() {
        Permanent kaleidoscope = harness.addToBattlefieldAndReturn(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(kaleidoscope.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Prism");
    }

    @Test
    @DisplayName("A tapped Kaleidoscope cannot create another Prism")
    void cannotCreateAnotherPrismWhileTapped() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Prism")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Prism can be sacrificed through a different Kaleidoscope")
    void sacrificesTappedPrismCreatedByAnotherKaleidoscope() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        Permanent otherKaleidoscope = harness.addToBattlefieldAndReturn(player1, new DiamondKaleidoscope());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent prism = findPermanent(player1, "Prism");
        prism.setTapped(true);
        otherKaleidoscope.setTapped(true);

        harness.activateAbility(player1, 1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Prism");
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Prism cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPrism() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        harness.addToBattlefield(player2, new DiamondKaleidoscope());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Prism");
        assertThat(gd.stack).isEmpty();
    }
}
