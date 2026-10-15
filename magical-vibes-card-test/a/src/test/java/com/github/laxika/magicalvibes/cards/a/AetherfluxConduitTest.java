package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherfluxConduit.class, Divination.class, GrizzlyBears.class, Ornithopter.class})
class AetherfluxConduitTest extends BaseCardTest {

    @Test
    void gainsEnergyEqualToManaSpentOnEachSpell() {
        harness.addToBattlefield(player1, new AetherfluxConduit());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysFiftyEnergyDrawsSevenAndOffersFreeSpellsFromHand() {
        Permanent conduit = harness.addToBattlefieldAndReturn(player1, new AetherfluxConduit());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        gd.playerEnergyCounters.put(player1.getId(), 50);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .hasSize(8).contains(ornithopter.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMayAbilityChosen(player1, true);
        for (int i = 0; i < 7; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(ornithopter.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(conduit);
    }

    @Test
    void cannotActivateWithoutFiftyEnergy() {
        Permanent conduit = harness.addToBattlefieldAndReturn(player1, new AetherfluxConduit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fifty energy counters");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(conduit);
    }
}
