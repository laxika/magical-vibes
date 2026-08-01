package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiamondKaleidoscopeTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T} creates a 0/1 colorless Prism artifact creature token")
    void createsPrismToken() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());
        Permanent kaleidoscope = gd.playerBattlefields.get(player1.getId()).getFirst();
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
    @DisplayName("Cannot sacrifice for mana with no Prism token")
    void cannotSacrificeWithoutPrism() {
        harness.addToBattlefield(player1, new DiamondKaleidoscope());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
