package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptwailing.class, DaggerclawImp.class, IzzetSignet.class})
class CryptwailingTest extends BaseCardTest {

    @Test
    void exilesTwoCreatureCardsAndMakesTargetPlayerDiscard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player2, List.of(new DaggerclawImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void targetPlayerChoosesWhichCardToDiscard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        DaggerclawImp keptCard = new DaggerclawImp();
        DaggerclawImp discardedCard = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player2, List.of(keptCard, discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new Cryptwailing());
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        DaggerclawImp discardedCard = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
    }

    @Test
    void canChooseCreatureCardsFromMixedGraveyard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        IzzetSignet nonCreatureCard = new IzzetSignet();
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(nonCreatureCard, firstCreature, secondCreature));
        harness.setHand(player2, List.of(new DaggerclawImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreatureCard);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
    }

    @Test
    void choosesExactlyTwoCreatureCardsFromLargerGraveyard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        IzzetSignet nonCreatureCard = new IzzetSignet();
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        DaggerclawImp thirdCreature = new DaggerclawImp();
        DaggerclawImp discardedCard = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(nonCreatureCard, firstCreature, secondCreature, thirdCreature));
        harness.setHand(player2, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), thirdCreature.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonCreatureCard, secondCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, thirdCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Daggerclaw Imp");
    }

    @Test
    void resolvesWithoutAHandCardToDiscard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        DaggerclawImp firstCreature = new DaggerclawImp();
        DaggerclawImp secondCreature = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithoutTwoCreatureCards() {
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, List.of(new DaggerclawImp(), new IzzetSignet()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyBeActivatedAsASorcery() {
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, List.of(new DaggerclawImp(), new DaggerclawImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetMustBeAPlayer() {
        harness.addToBattlefield(player1, new Cryptwailing());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        harness.setGraveyard(player1, List.of(new DaggerclawImp(), new DaggerclawImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
