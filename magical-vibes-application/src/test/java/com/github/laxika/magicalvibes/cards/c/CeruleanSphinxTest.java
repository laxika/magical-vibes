package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CeruleanSphinx.class)
class CeruleanSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} shuffles Cerulean Sphinx into its owner's library")
    void activateShufflesIntoOwnersLibrary() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new CeruleanSphinx());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sphinx);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof CeruleanSphinx);
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof CeruleanSphinx);
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {U}")
    void requiresMana() {
        harness.addToBattlefield(player1, new CeruleanSphinx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
