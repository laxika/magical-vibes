package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CallTheScions.class)
class CallTheScionsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Call the Scions creates two Eldrazi Scion tokens")
    void createsTwoEldraziScions() {
        castCallTheScions();

        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        assertThat(scions).hasSize(2);
        assertThat(scions).allSatisfy(scion -> {
            assertThat(scion.getCard().isToken()).isTrue();
            assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(scion.getCard().getColor()).isNull();
            assertThat(scion.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("An Eldrazi Scion token can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        castCallTheScions();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    private void castCallTheScions() {
        harness.setHand(player1, List.of(new CallTheScions()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
