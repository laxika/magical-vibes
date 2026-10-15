package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BroodMonitor.class)
class BroodMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("When Brood Monitor enters, it creates three Eldrazi Scion tokens")
    void enteringCreatesThreeEldraziScions() {
        castBroodMonitor();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(3);
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed to add colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        castBroodMonitor();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("Scions are created only when the enter trigger resolves")
    void tokensWaitForEnterTriggerToResolve() {
        harness.setHand(player1, List.of(new BroodMonitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Brood Monitor")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(3);
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("All three newly created Scions can produce mana while tapped")
    void tappedScionsCanEachBeSacrificedImmediately() {
        castBroodMonitor();

        for (Permanent scion : findPermanents(player1, "Eldrazi Scion")) {
            scion.tap();
            int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
            harness.activateAbility(player1, scionIndex, 0, null, null);
            assertThat(gd.stack).isEmpty();
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player1, "Brood Monitor")).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The created Scions are untapped colorless 1/1 Eldrazi Scion creatures")
    void createdTokensHaveOracleCharacteristics() {
        castBroodMonitor();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(3).allSatisfy(scion -> {
            assertThat(scion.getCard().isToken()).isTrue();
            assertThat(gqs.isCreature(gd, scion)).isTrue();
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, scion)).isEmpty();
            assertThat(scion.getCard().getSubtypes()).containsExactlyInAnyOrder(
                    CardSubtype.ELDRAZI,
                    CardSubtype.SCION);
            assertThat(scion.isTapped()).isFalse();
        });
    }
    private void castBroodMonitor() {
        harness.setHand(player1, List.of(new BroodMonitor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
