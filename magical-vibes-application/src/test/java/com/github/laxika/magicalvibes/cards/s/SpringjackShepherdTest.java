package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpringjackShepherdTest extends BaseCardTest {

    private List<Permanent> goats(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Goat"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates a Goat for each white mana symbol among your permanents (self included)")
    void etbCreatesGoatPerWhiteSymbol() {
        // Suntail Hawk {W} = 1 white symbol; Elite Vanguard {W} = 1; Grizzly Bears {1}{G} = 0.
        // Springjack Shepherd itself {3}{W} = 1 (on the battlefield when the trigger resolves). Total = 3.
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Springjack Shepherd, queue ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = goats(player1);
        assertThat(tokens).hasSize(3);

        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOAT);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Non-white permanents contribute nothing; only the Shepherd's own white symbol counts")
    void etbCountsOnlyWhiteSymbols() {
        addCreatureReady(player1, new GrizzlyBears()); // {1}{G} = 0 white symbols

        harness.setHand(player1, List.of(new SpringjackShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(goats(player1)).hasSize(1);
    }
}
