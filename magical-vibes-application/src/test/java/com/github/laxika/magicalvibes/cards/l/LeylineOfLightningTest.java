package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfLightning.class, ChandraNalaar.class, GhostWarden.class})
class LeylineOfLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfLightning()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Lightning"));
    }

    @Test
    @DisplayName("Declining the opening-hand choice leaves Leyline in hand")
    void decliningOpeningHandChoiceLeavesLeylineInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfLightning leyline = new LeylineOfLightning();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of Lightning"));
        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId()))
                .containsExactly(leyline);
    }

    @Test
    @DisplayName("Paying {1} after casting a spell deals 1 damage to a player")
    void payingManaDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        harness.setHand(player1, List.of(new GhostWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Leyline damage can target a planeswalker")
    void payingManaDealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new GhostWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the payment deals no damage")
    void decliningPaymentDealsNoDamage() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        harness.setHand(player1, List.of(new GhostWarden()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Leyline")
    void opponentsSpellDoesNotTriggerLeyline() {
        harness.addToBattlefield(player1, new LeylineOfLightning());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GhostWarden()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
