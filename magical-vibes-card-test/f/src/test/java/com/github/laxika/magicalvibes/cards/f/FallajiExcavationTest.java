package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallajiExcavation.class})
class FallajiExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three tapped Powerstones and you gain 3 life")
    void createsPowerstonesAndGainsLife() {
        harness.setHand(player1, List.of(new FallajiExcavation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(3);
        assertThat(powerstones).allMatch(powerstone -> powerstone.isTapped()
                && powerstone.getCard().hasType(CardType.ARTIFACT)
                && powerstone.getCard().getSubtypes().contains(CardSubtype.POWERSTONE));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Powerstones add colorless mana immediately without using the stack")
    void powerstonesProduceRestrictedMana() {
        harness.setHand(player1, List.of(new FallajiExcavation(), new FallajiExcavation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);

        harness.performUntapStep(player1);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, i, null, null);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(3)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 23);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(6);
        harness.assertLife(player1, 26);
    }
}
