package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptwailing.class, GrizzlyBears.class, Forest.class})
class CryptwailingTest extends BaseCardTest {

    @Test
    void exilesTwoCreatureCardsAndMakesTargetPlayerDiscard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canChooseCreatureCardsFromMixedGraveyard() {
        harness.addToBattlefield(player1, new Cryptwailing());
        Forest forest = new Forest();
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, firstCreature, secondCreature));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstCreature, secondCreature);
    }

    @Test
    void cannotActivateWithoutTwoCreatureCards() {
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canOnlyBeActivatedAsASorcery() {
        harness.addToBattlefield(player1, new Cryptwailing());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
