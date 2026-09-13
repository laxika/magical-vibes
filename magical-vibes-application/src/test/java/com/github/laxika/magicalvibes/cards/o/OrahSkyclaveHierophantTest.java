package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TaboraxHopesDemise;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrahSkyclaveHierophant.class, ExpeditionHealer.class, GrizzlyBears.class,
        TaboraxHopesDemise.class})
class OrahSkyclaveHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a lesser Cleric when another Cleric you control dies")
    void returnsLesserClericWhenAnotherClericDies() {
        Card eligible = new ExpeditionHealer();
        Card equalManaValue = new TaboraxHopesDemise();
        Card nonCleric = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible, equalManaValue, nonCleric));
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCleric = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());

        destroy(dyingCleric);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Taborax, Hope's Demise");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(eligible.getId());
    }

    @Test
    @DisplayName("Does not trigger when a non-Cleric you control dies")
    void doesNotTriggerForNonClericDeath() {
        Card eligible = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(dyingCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a lesser Cleric when Orah dies")
    void returnsLesserClericWhenOrahDies() {
        Card eligible = new ExpeditionHealer();
        Card equalManaValue = new OrahSkyclaveHierophant();
        harness.setGraveyard(player1, List.of(eligible, equalManaValue));
        Permanent orah = harness.addToBattlefieldAndReturn(player1, new OrahSkyclaveHierophant());

        destroy(orah);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Orah, Skyclave Hierophant");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(equalManaValue.getId());
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
