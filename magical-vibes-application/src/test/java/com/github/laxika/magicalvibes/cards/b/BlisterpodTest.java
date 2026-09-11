package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({Blisterpod.class, WrathOfGod.class})
class BlisterpodTest extends BaseCardTest {

    @Test
    @DisplayName("When Blisterpod dies, it creates an Eldrazi Scion token")
    void deathTriggerCreatesEldraziScion() {
        destroyBlisterpod();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(scion.getCard().getColor()).isNull();
        assertThat(scion.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(scion.getCard().getPower()).isEqualTo(1);
        assertThat(scion.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Eldrazi Scion token can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        destroyBlisterpod();

        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanents(player1, "Eldrazi Scion").getFirst());
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    private void destroyBlisterpod() {
        harness.addToBattlefield(player1, new Blisterpod());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
