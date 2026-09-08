package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FallajiExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three tapped Powerstones and you gain 3 life")
    void createsPowerstonesAndGainsLife() {
        harness.setHand(player1, List.of(new FallajiExcavation()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(3);
        assertThat(powerstones).allMatch(powerstone -> powerstone.isTapped()
                && powerstone.getCard().hasType(CardType.ARTIFACT)
                && powerstone.getCard().getSubtypes().contains(CardSubtype.POWERSTONE));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }
}
