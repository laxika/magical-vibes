package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LotusCobra;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JwariDisruption.class, JwariRuins.class, LotusCobra.class, Forest.class})
class JwariDisruptionTest extends BaseCardTest {

    @Test
    void countersSpellWhenItsControllerCannotPay() {
        harness.forceActivePlayer(player2);
        LotusCobra cobra = new LotusCobra();
        harness.castFromHand(player2, cobra, "{1}{G}");

        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, cobra.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lotus Cobra");
        harness.assertNotOnBattlefield(player2, "Lotus Cobra");
    }

    @Test
    void spellResolvesWhenItsControllerPays() {
        harness.forceActivePlayer(player2);
        LotusCobra cobra = new LotusCobra();
        harness.castFromHand(player2, cobra, "{1}{G}");
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, cobra.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Lotus Cobra");
    }

    @Test
    void counterModeCannotTargetALand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceEntersTappedAndProducesBlueMana() {
        harness.setHand(player1, List.of(new JwariDisruption()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(JwariRuins.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void countersSpellWhenItsControllerDeclinesPayment() {
        harness.forceActivePlayer(player2);
        LotusCobra cobra = new LotusCobra();
        harness.castFromHand(player2, cobra, "{1}{G}");
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, cobra.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Lotus Cobra");
        harness.assertNotOnBattlefield(player2, "Lotus Cobra");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void offersPaymentWhenManaCanBeProducedDuringResolution() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new Forest());
        LotusCobra cobra = new LotusCobra();
        harness.castFromHand(player2, cobra, "{1}{G}");
        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, cobra.getId());

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Lotus Cobra");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void landFaceUsesTheLandPlayForTheTurn() {
        harness.setHand(player1, List.of(new JwariDisruption(), new Forest()));
        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void landFaceCannotBePlayedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new JwariDisruption()));
        harness.passPriority(player2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Jwari Ruins");
        harness.assertInHand(player1, "Jwari Disruption");
    }
}
