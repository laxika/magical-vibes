package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MastersCallTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates two 1/1 colorless Myr artifact creature tokens")
    void resolvingCreatesTwoMyrTokens() {
        harness.setHand(player1, List.of(new MastersCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> myrs = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .toList();
        assertThat(myrs).hasSize(2);

        for (Permanent myr : myrs) {
            assertThat(myr.getCard().getPower()).isEqualTo(1);
            assertThat(myr.getCard().getToughness()).isEqualTo(1);
            assertThat(myr.getCard().getColor()).isNull();
            assertThat(myr.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(myr.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(myr.getCard().getSubtypes()).contains(CardSubtype.MYR);
        }
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new MastersCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Master's Call");
    }
}
