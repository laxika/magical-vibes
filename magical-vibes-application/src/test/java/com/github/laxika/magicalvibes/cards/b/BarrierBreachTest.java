package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrierBreach.class, AngelicChorus.class, AuraOfSilence.class, GloriousAnthem.class, GrizzlyBears.class})
class BarrierBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three target enchantments")
    void exilesThreeTargetEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        castBarrierBreach(List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Angelic Chorus"))
                .anyMatch(card -> card.getName().equals("Aura of Silence"));
    }

    @Test
    @DisplayName("Can choose fewer than three enchantments")
    void canChooseFewerEnchantments() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new AngelicChorus());

        castBarrierBreach(List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can choose no enchantments")
    void canChooseNoEnchantments() {
        harness.addToBattlefield(player2, new GloriousAnthem());

        castBarrierBreach(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a nonenchantment permanent")
    void cannotTargetNonenchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Barrier Breach and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BarrierBreach()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Barrier Breach");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castBarrierBreach(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BarrierBreach()));
        addManaForSpell();
        harness.castInstant(player1, 0, targetIds);
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
