package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EyelessWatcher.class})
class EyelessWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When Eyeless Watcher enters, it creates two Eldrazi Scion tokens")
    void enteringCreatesTwoEldraziScions() {
        castEyelessWatcher();

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
    @DisplayName("The Eldrazi Scion tokens can be sacrificed for colorless mana")
    void scionsCanBeSacrificedForColorlessMana() {
        castEyelessWatcher();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("Scions are created by the entry trigger, not by casting or resolving the creature spell")
    void tokensWaitForEntryTriggerToResolve() {
        harness.castFromHand(player1, new EyelessWatcher(), "{3}{G}");

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eyeless Watcher")).hasSize(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
        assertThat(findPermanents(player2, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both newly created Scions can be sacrificed while tapped, adding mana immediately")
    void bothTappedScionsCanProduceManaImmediately() {
        castEyelessWatcher();

        for (Permanent scion : findPermanents(player1, "Eldrazi Scion")) {
            scion.setTapped(true);
            int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
            harness.activateAbility(player1, scionIndex, 0, null, null);
            assertThat(gd.stack).isEmpty();
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(findPermanents(player1, "Eyeless Watcher")).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void castEyelessWatcher() {
        harness.castFromHand(player1, new EyelessWatcher(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
