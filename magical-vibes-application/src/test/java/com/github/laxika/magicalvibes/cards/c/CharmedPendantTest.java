package com.github.laxika.magicalvibes.cards.c;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CharmedPendantTest extends BaseCardTest {

    @Test
    @DisplayName("Mills a card and adds mana for each colored symbol in its mana cost")
    void millsAndAddsManaForEachColoredSymbol() {
        harness.forceActivePlayer(player1);
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new CruelUltimatum();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(pendant.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(pool.get(ManaColor.WHITE)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(pool.get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not add mana for a card with no colored mana symbols")
    void doesNotAddManaForColorlessCost() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        Card milled = new Mountain();
        harness.setLibrary(player1, List.of(milled));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be activated when the library is empty")
    void cannotActivateWithEmptyLibrary() {
        harness.forceActivePlayer(player1);
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new CharmedPendant());
        harness.setLibrary(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to mill");

        assertThat(pendant.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
