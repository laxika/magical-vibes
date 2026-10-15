package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EarthSurge;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheMeek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalLight.class, EarthSurge.class, LeylineOfTheMeek.class, Gristleback.class})
class PrimevalLightTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only enchantments controlled by the target player")
    void destroysOnlyTargetPlayersEnchantments() {
        harness.addToBattlefield(player1, new EarthSurge());
        harness.addToBattlefield(player2, new EarthSurge());
        harness.addToBattlefield(player2, new LeylineOfTheMeek());
        harness.addToBattlefield(player2, new Gristleback());

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Earth Surge");
        harness.assertNotOnBattlefield(player2, "Earth Surge");
        harness.assertNotOnBattlefield(player2, "Leyline of the Meek");
        harness.assertOnBattlefield(player2, "Gristleback");
    }

    @Test
    @DisplayName("Can target the caster")
    void canTargetCaster() {
        harness.addToBattlefield(player1, new EarthSurge());
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.addToBattlefield(player2, new EarthSurge());

        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Earth Surge");
        harness.assertNotOnBattlefield(player1, "Leyline of the Meek");
        harness.assertOnBattlefield(player2, "Earth Surge");
    }

    @Test
    @DisplayName("Can target a player who controls no enchantments")
    void canTargetPlayerWithoutEnchantments() {
        harness.addToBattlefield(player1, new EarthSurge());
        harness.addToBattlefield(player2, new Gristleback());
        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Earth Surge");
        harness.assertOnBattlefield(player2, "Gristleback");
        harness.assertInGraveyard(player1, "Primeval Light");
    }

    @Test
    @DisplayName("Destroys enchantments that entered after the spell was cast")
    void determinesEnchantmentsAtResolution() {
        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, player2.getId());

        harness.addToBattlefield(player2, new EarthSurge());
        harness.addToBattlefield(player1, new LeylineOfTheMeek());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Earth Surge");
        harness.assertNotOnBattlefield(player2, "Earth Surge");
        harness.assertOnBattlefield(player1, "Leyline of the Meek");
        harness.assertInGraveyard(player1, "Primeval Light");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player2, new Gristleback());
        harness.setHand(player1, List.of(new PrimevalLight()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        var permanentId = findPermanent(player2, "Gristleback").getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
