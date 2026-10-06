package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.n.NimbusWings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeismicShudder.class, FugitiveWizard.class, SuntailHawk.class,
        KrakenHatchling.class, NimbusWings.class})
class SeismicShudderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature without flying")
    void damagesOnlyCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SeismicShudder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new SeismicShudder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Marks exactly 1 damage on nonflying creatures controlled by either player")
    void damagesNonflyingCreaturesOnBothSides() {
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new KrakenHatchling());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());
        harness.setHand(player1, List.of(new SeismicShudder()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Kraken Hatchling");
        harness.assertOnBattlefield(player2, "Kraken Hatchling");
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spared creatures include those with flying granted by an Aura")
    void doesNotDamageCreatureWithGrantedFlying() {
        var creature = harness.addToBattlefieldAndReturn(player1, new KrakenHatchling());
        harness.setHand(player1, List.of(new NimbusWings(), new SeismicShudder()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Nimbus Wings");
        harness.assertOnBattlefield(player1, "Kraken Hatchling");
        assertThat(creature.getMarkedDamage()).isZero();
    }
}
