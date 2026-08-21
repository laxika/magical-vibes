package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValkiGodOfLies.class, TibaltCosmicImpostor.class, Forest.class, GrizzlyBears.class})
class ValkiGodOfLiesTest extends BaseCardTest {

    @Test
    void entersAndExilesAnOpponentCreatureUntilValkiLeaves() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest nonCreature = new Forest();
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        harness.setHand(player2, List.of(creature, nonCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent valki = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, valki));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(nonCreature, creature);
        assertThat(gd.getCardsExiledByPermanent(valki.getId())).isEmpty();
    }

    @Test
    void becomesACopyOfAQualifyingExiledCreature() {
        Permanent valki = harness.addToBattlefieldAndReturn(player1, new ValkiGodOfLies());
        GrizzlyBears exiledCreature = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiledCreature, valki.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int valkiIndex = gd.playerBattlefields.get(player1.getId()).indexOf(valki);
        harness.activateAbility(player1, valkiIndex, 0, 2, null);
        harness.passBothPriorities();

        assertThat(valki.getCard().getName()).isEqualTo(exiledCreature.getName());
        assertThat(gqs.getEffectivePower(gd, valki)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, valki)).isEqualTo(2);
    }

    @Test
    void TibaltCreatesThePlayPermissionEmblemAndTracksEachLibraryCard() {
        harness.setHand(player1, List.of(new ValkiGodOfLies()));
        Forest playerOneTop = new Forest();
        GrizzlyBears playerTwoTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(playerOneTop));
        harness.setLibrary(player2, List.of(playerTwoTop));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tibalt = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);

        int tibaltIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tibalt);
        harness.activateAbility(player1, tibaltIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(tibalt.getId()))
                .containsExactly(playerOneTop, playerTwoTop);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, tibalt));

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, playerTwoTop.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(com.github.laxika.magicalvibes.model.Card::getName))
                .contains(playerTwoTop.getName());
    }
}
