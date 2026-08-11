package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskOfImmolationTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Mask of Immolation creates and attaches a red Elemental token")
    void enteringCreatesAndAttachesElemental() {
        harness.setHand(player1, List.of(new MaskOfImmolation()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Mask of Immolation");
        Permanent elemental = findPermanent(player1, "Elemental");

        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(elemental.getCard().getPower()).isEqualTo(1);
        assertThat(elemental.getCard().getToughness()).isEqualTo(1);
        assertThat(elemental.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(mask.getAttachedTo()).isEqualTo(elemental.getId());
    }

    @Test
    @DisplayName("Equipped creature can sacrifice itself to deal 1 damage to a player")
    void equippedCreatureCanSacrificeItselfToDealDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MaskOfImmolation()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        int elementalIndex = gd.playerBattlefields.get(player1.getId()).indexOf(elemental);

        harness.activateAbility(player1, elementalIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Elemental");
        harness.assertOnBattlefield(player1, "Mask of Immolation");
    }
}
