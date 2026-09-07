package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeOutTheTrash.class, ChandraBoldPyromancer.class, FountainOfYouth.class,
        Forest.class, GrizzlyBears.class, RaccoonRallier.class})
class TakeOutTheTrashTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToCreatureAndDoesNotOfferLootWithoutRaccoon() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card keeper = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new TakeOutTheTrash(), keeper)));
        addMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
    }

    @Test
    void offersLootWhenYouControlARaccoonAndDrawsAfterDiscard() {
        harness.addToBattlefield(player1, new RaccoonRallier());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest draw = new Forest();
        Card discard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, new ArrayList<>(List.of(new TakeOutTheTrash(), discard)));
        addMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void decliningLootDoesNotDiscardOrDraw() {
        harness.addToBattlefield(player1, new RaccoonRallier());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card keeper = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new TakeOutTheTrash(), keeper)));
        addMana();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(keeper.getId()));
    }

    @Test
    void canTargetAPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new TakeOutTheTrash()));
        addMana();

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void cannotTargetAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TakeOutTheTrash()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
