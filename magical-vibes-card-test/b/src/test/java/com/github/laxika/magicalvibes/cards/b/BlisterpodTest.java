package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.p.PlanarOutburst;
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

@CardUsed({Blisterpod.class, PlanarOutburst.class, CompleteDisregard.class})
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
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
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
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each dying Blisterpod creates a token for its controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new Blisterpod());
        harness.addToBattlefield(player1, new Blisterpod());
        harness.addToBattlefield(player2, new Blisterpod());

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blisterpod")).isEmpty();
        assertThat(findPermanents(player2, "Blisterpod")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("Exiling Blisterpod does not trigger its death ability")
    void exileDoesNotCreateToken() {
        Permanent blisterpod = harness.addToBattlefieldAndReturn(player2, new Blisterpod());
        harness.setHand(player1, List.of(new CompleteDisregard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, blisterpod.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Blisterpod")).isEmpty();
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(blisterpod.getCard().getId()));
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    private void destroyBlisterpod() {
        harness.addToBattlefield(player1, new Blisterpod());

        harness.castFromHand(player1, new PlanarOutburst(), "{3}{W}{W}");
        resolveAllTriggers();
    }
}
