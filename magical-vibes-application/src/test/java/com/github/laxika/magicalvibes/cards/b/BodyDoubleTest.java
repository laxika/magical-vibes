package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyDouble.class, GossamerPhantasm.class, Pongify.class, AvenRiftwatcher.class})
class BodyDoubleTest extends BaseCardTest {

    @Test
    void entersAsACopyOfACreatureCardInAnyGraveyard() {
        Card creature = new GossamerPhantasm();
        harness.setGraveyard(player2, List.of(creature));
        castBodyDouble();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent bodyDouble = findBodyDouble();
        assertThat(bodyDouble).isNotNull();
        assertThat(bodyDouble.getCard().getName()).isEqualTo("Gossamer Phantasm");
        assertThat(bodyDouble.getCard().getPower()).isEqualTo(2);
        assertThat(bodyDouble.getCard().getToughness()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
    }

    @Test
    void onlyCreatureCardsInAnyGraveyardAreEligible() {
        Card nonCreature = new Pongify();
        Card creature = new GossamerPhantasm();
        harness.setGraveyard(player2, List.of(nonCreature, creature));
        castBodyDouble();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent bodyDouble = findBodyDouble();
        assertThat(bodyDouble).isNotNull();
        assertThat(bodyDouble.getCard().getName()).isEqualTo("Gossamer Phantasm");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonCreature, creature);
    }

    @Test
    void entersWithoutCopyingWhenNoCreatureCardIsInAnyGraveyard() {
        Card nonCreature = new Pongify();
        harness.setGraveyard(player2, List.of(nonCreature));
        castBodyDouble();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Body Double");
        harness.assertInGraveyard(player1, "Body Double");
    }

    @Test
    void mayDeclineToCopy() {
        harness.setGraveyard(player2, List.of(new GossamerPhantasm()));
        castBodyDouble();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Body Double");
    }

    @Test
    void canChooseItsControllersGraveyardWhenBothGraveyardsContainCreatures() {
        Card ownCreature = new AvenRiftwatcher();
        Card opposingCreature = new GossamerPhantasm();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        castBodyDouble();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aven Riftwatcher");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    void copiesEntryReplacementAndEnterAndLeaveTriggersButReturnsToGraveyardAsBodyDouble() {
        Card creature = new AvenRiftwatcher();
        harness.setGraveyard(player2, List.of(creature));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castBodyDouble();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        Permanent copy = findBodyDouble();
        assertThat(copy).isNotNull();
        assertThat(copy.getCounterCount(CounterType.TIME)).isEqualTo(3);
        harness.assertLife(player1, lifeBefore + 2);

        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, copy.getId());
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertInGraveyard(player1, "Body Double");
        harness.assertNotInGraveyard(player1, "Aven Riftwatcher");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void copiesTriggeredAbilityThatSacrificesItWhenTargeted() {
        Card creature = new GossamerPhantasm();
        harness.setGraveyard(player2, List.of(creature));
        castBodyDouble();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        Permanent copy = findBodyDouble();
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, copy.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertNotOnBattlefield(player1, "Ape");
        harness.assertInGraveyard(player1, "Body Double");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    private void castBodyDouble() {
        harness.castFromHand(player1, new BodyDouble(), "{4}{U}");
    }

    private Permanent findBodyDouble() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BodyDouble)
                .findFirst()
                .orElse(null);
    }
}
