package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating creates a 0/1 blue Plant Wall token with defender named Kelp")
    void createsKelpToken() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        Permanent kelp = findPermanent(player1, "Kelp");
        assertThat(kelp.getCard().getPower()).isEqualTo(0);
        assertThat(kelp.getCard().getToughness()).isEqualTo(1);
        assertThat(kelp.getCard().getSubtypes()).contains(CardSubtype.PLANT, CardSubtype.WALL);
        assertThat(kelp.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("Ability cannot be activated without the mana")
    void requiresMana() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Kelp");
        assertThat(wall.isTapped()).isFalse();
    }
}
