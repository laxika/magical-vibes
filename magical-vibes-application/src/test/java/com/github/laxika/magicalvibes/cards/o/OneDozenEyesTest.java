package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OneDozenEyesTest extends BaseCardTest {

    @Test
    @DisplayName("The Beast mode creates one 5/5 green Beast token")
    void createsBeastToken() {
        cast(new int[]{0}, false);

        List<Permanent> beasts = findPermanents(player1, "Beast");
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().isToken()).isTrue();
        assertThat(beasts.getFirst().getCard().getPower()).isEqualTo(5);
        assertThat(beasts.getFirst().getCard().getToughness()).isEqualTo(5);
        assertThat(beasts.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("The Insect mode creates five 1/1 green Insect tokens")
    void createsInsectTokens() {
        cast(new int[]{1}, false);

        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(5);
        assertThat(insects).allSatisfy(insect -> {
            assertThat(insect.getCard().isToken()).isTrue();
            assertThat(insect.getCard().getPower()).isEqualTo(1);
            assertThat(insect.getCard().getToughness()).isEqualTo(1);
            assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        });
    }

    @Test
    @DisplayName("Entwine creates both the Beast and Insect tokens")
    void entwinesBothModes() {
        cast(new int[]{0, 1}, true);

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(findPermanents(player1, "Insect")).hasSize(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine requires the additional three green mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new OneDozenEyes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined) {
        harness.setHand(player1, List.of(new OneDozenEyes()));
        harness.addMana(player1, ManaColor.GREEN, entwined ? 4 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
