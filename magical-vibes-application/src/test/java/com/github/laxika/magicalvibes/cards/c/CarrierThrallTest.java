package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
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

@CardUsed({CarrierThrall.class, WrathOfGod.class, PathToExile.class})
class CarrierThrallTest extends BaseCardTest {

    @Test
    @DisplayName("When Carrier Thrall dies, it creates an Eldrazi Scion token")
    void deathCreatesEldraziScion() {
        killCarrierThrall();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("The death trigger creates a 1/1 colorless Eldrazi Scion creature token")
    void createdTokenHasOracleCharacteristics() {
        killCarrierThrall();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(scion.getCard().getPower()).isEqualTo(1);
        assertThat(scion.getCard().getToughness()).isEqualTo(1);
        assertThat(scion.getCard().getColors()).isEmpty();
        assertThat(scion.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
    }

    @Test
    @DisplayName("The Eldrazi Scion can be sacrificed to add colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        killCarrierThrall();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Each dying Carrier Thrall creates a Scion for its controller after the sweeper resolves")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new CarrierThrall());
        harness.addToBattlefield(player2, new CarrierThrall());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
        harness.assertInGraveyard(player1, "Carrier Thrall");
        harness.assertInGraveyard(player2, "Carrier Thrall");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Carrier Thrall does not create a Scion")
    void exileDoesNotTriggerDeathAbility() {
        harness.addToBattlefield(player1, new CarrierThrall());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Carrier Thrall"));

        harness.assertNotOnBattlefield(player1, "Carrier Thrall");
        harness.assertNotInGraveyard(player1, "Carrier Thrall");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Carrier Thrall"));
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void killCarrierThrall() {
        harness.addToBattlefield(player1, new CarrierThrall());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
