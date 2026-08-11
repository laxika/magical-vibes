package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HellsparkElemental;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandrasEmbercatTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability produces mana that casts an Elemental spell")
    void manaCastsElementalSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new HellsparkElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hellspark Elemental");
    }

    @Test
    @DisplayName("Mana ability produces mana that casts a Chandra planeswalker spell")
    void manaCastsChandraPlaneswalkerSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new ChandraNalaar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castPlaneswalker(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Mana ability cannot pay for a non-Elemental or non-Chandra spell")
    void manaCannotCastUnlistedSpell() {
        addCreatureReady(player1, new ChandrasEmbercat());
        harness.setHand(player1, List.of(new JaceBeleren()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
