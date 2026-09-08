package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WandOfTheElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island creates a 2/2 blue Elemental with flying")
    void islandAbilityCreatesBlueElemental() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Sacrificing a Mountain creates a 3/3 red Elemental")
    void mountainAbilityCreatesRedElemental() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Each ability requires the matching basic land type")
    void abilitiesRequireMatchingLandType() {
        harness.addToBattlefield(player1, new WandOfTheElements());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
