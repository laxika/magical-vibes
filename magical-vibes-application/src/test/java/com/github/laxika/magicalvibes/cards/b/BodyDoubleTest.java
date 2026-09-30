package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyDouble.class, GossamerPhantasm.class, Pongify.class})
class BodyDoubleTest extends BaseCardTest {

    @Test
    void entersAsACopyOfACreatureCardInAnyGraveyard() {
        Card creature = new GossamerPhantasm();
        harness.setGraveyard(player2, List.of(creature));
        castBodyDouble();

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        harness.passBothPriorities();
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

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Body Double");
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
