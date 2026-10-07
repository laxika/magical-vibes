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

        harness.castAndResolveInstant(player1, 0, creature.getId());

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

        harness.castAndResolveInstant(player1, 0, creature.getId());

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

        harness.castAndResolveInstant(player1, 0, creature.getId());
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

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

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

    @Test
    void opponentsRaccoonDoesNotEnableLoot() {
        Permanent raccoon = harness.addToBattlefieldAndReturn(player2, new RaccoonRallier());
        Card keeper = new Forest();
        harness.setHand(player1, List.of(new TakeOutTheTrash(), keeper));
        addMana();

        harness.castAndResolveInstant(player1, 0, raccoon.getId());

        harness.assertInGraveyard(player2, "Raccoon Rallier");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
    }

    @Test
    void acceptingLootWithAnEmptyHandDoesNotDraw() {
        harness.addToBattlefield(player1, new RaccoonRallier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaccoonRallier());
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new TakeOutTheTrash()));
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canLootWhenTheOnlyRaccoonReceivesLethalDamageFromThisSpell() {
        Permanent raccoon = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        Card discard = new Forest();
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new TakeOutTheTrash(), discard));
        addMana();

        harness.castAndResolveInstant(player1, 0, raccoon.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertOnBattlefield(player1, "Raccoon Rallier");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        harness.assertInGraveyard(player1, "Raccoon Rallier");
    }

    @Test
    void illegalTargetPreventsLootEvenWithARaccoon() {
        harness.addToBattlefield(player1, new RaccoonRallier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RaccoonRallier());
        Card keeper = new Forest();
        Card draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new TakeOutTheTrash(), keeper));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keeper);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Take Out the Trash");
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
