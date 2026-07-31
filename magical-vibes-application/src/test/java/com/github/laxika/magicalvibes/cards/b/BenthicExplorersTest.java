package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BenthicExplorersTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping the only tapped opponent land untaps it and adds that land's mana")
    void untapsOpponentLandAndAddsItsMana() {
        addReadyExplorers();
        harness.addToBattlefield(player2, new Island());
        harness.tapPermanent(player2, 0);
        UUID islandId = harness.getPermanentId(player2, "Island");

        harness.activateAbility(player1, 0, null, null);

        assertThat(isTapped(islandId)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot be activated when no opponent land is tapped")
    void cannotActivateWithoutTappedOpponentLand() {
        addReadyExplorers();
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped land the controller controls is not a legal payment")
    void ownTappedLandIsNotALegalPayment() {
        addReadyExplorers();
        harness.addToBattlefield(player1, new Island());
        harness.tapPermanent(player1, 1);
        UUID ownIslandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        // Tapping it for mana above added {U}; the cost may not untap it to add a second mana
        assertThat(isTapped(ownIslandId)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("With several tapped opponent lands the chosen land decides the mana type")
    void chosenLandDecidesManaType() {
        addReadyExplorers();
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(harness.getPermanentId(player2, "Island"), forestId);

        harness.handlePermanentChosen(player1, forestId);

        assertThat(isTapped(forestId)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    private void addReadyExplorers() {
        Permanent explorers = new Permanent(new BenthicExplorers());
        explorers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(explorers);
    }

    private boolean isTapped(UUID permanentId) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getId().equals(permanentId))
                .findFirst()
                .map(Permanent::isTapped)
                .orElseThrow();
    }
}
