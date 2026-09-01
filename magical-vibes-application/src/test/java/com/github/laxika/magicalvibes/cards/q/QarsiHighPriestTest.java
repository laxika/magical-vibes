package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QarsiHighPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Pays mana, taps, sacrifices another creature, and manifests the top card")
    void activatesAndManifestsTopCard() {
        Permanent priest = addCreatureReady(player1, new QarsiHighPriest());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        assertThat(priest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(priest).doesNotContain(fodder);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new QarsiHighPriest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature to sacrifice");
    }
}
