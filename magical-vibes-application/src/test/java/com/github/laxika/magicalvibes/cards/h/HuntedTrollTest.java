package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HuntedTroll.class)
class HuntedTrollTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates four blue 1/1 Faerie tokens with flying under the targeted opponent's control")
    void etbCreatesFaerieTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> faeries = findPermanents(player2, "Faerie");
        assertThat(faeries).hasSize(4);
        assertThat(findPermanents(player1, "Faerie")).isEmpty();

        for (Permanent faerie : faeries) {
            assertThat(faerie.getCard().isToken()).isTrue();
            assertThat(faerie.getCard().getPower()).isEqualTo(1);
            assertThat(faerie.getCard().getToughness()).isEqualTo(1);
            assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(faerie.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(faerie.getCard().getSubtypes()).containsExactly(CardSubtype.FAERIE);
            assertThat(gqs.hasKeyword(gd, faerie, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedTroll()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        harness.addToBattlefield(player1, new HuntedTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = findPermanent(player1, "Hunted Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }
}
