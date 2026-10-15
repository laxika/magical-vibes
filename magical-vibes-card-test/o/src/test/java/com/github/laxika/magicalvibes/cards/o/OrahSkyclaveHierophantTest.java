package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.c.CliffhavenSellSword;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
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

@CardUsed({OrahSkyclaveHierophant.class, ExpeditionHealer.class, CliffhavenSellSword.class,
        TaboraxHopesDemise.class, AmoeboidChangeling.class})
class OrahSkyclaveHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from Orah gains life")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new OrahSkyclaveHierophant());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Returns a lesser Cleric when another Cleric you control dies")
    void returnsLesserClericWhenAnotherClericDies() {
        Card eligible = new ExpeditionHealer();
        Card equalManaValue = new TaboraxHopesDemise();
        Card nonCleric = new CliffhavenSellSword();
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
        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(eligible.getId());
    }

    @Test
    @DisplayName("Does not trigger when a non-Cleric you control dies")
    void doesNotTriggerForNonClericDeath() {
        Card eligible = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new CliffhavenSellSword());

        destroy(dyingCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Cliffhaven Sell-Sword");
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

    @Test
    @DisplayName("Does not trigger when an opponent's Cleric dies")
    void doesNotTriggerForOpponentsCleric() {
        harness.setGraveyard(player1, List.of(new ExpeditionHealer()));
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCleric = harness.addToBattlefieldAndReturn(player2, new TaboraxHopesDemise());

        destroy(dyingCleric);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Expedition Healer");
        harness.assertInGraveyard(player2, "Taborax, Hope's Demise");
    }

    @Test
    @DisplayName("Only targets Clerics in its controller's graveyard")
    void excludesOpponentsGraveyard() {
        Card eligible = new ExpeditionHealer();
        Card opponentsCleric = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setGraveyard(player2, List.of(opponentsCleric));
        Permanent orah = harness.addToBattlefieldAndReturn(player1, new OrahSkyclaveHierophant());

        destroy(orah);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Expedition Healer");
        harness.assertInGraveyard(player2, "Expedition Healer");
    }

    @Test
    @DisplayName("Does not return an equal or greater mana value Cleric when none is eligible")
    void noEligibleTargetWhenLowestManaValueClericDies() {
        harness.setGraveyard(player1, List.of(new ExpeditionHealer(), new TaboraxHopesDemise()));
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCleric = harness.addToBattlefieldAndReturn(player1, new ExpeditionHealer());

        destroy(dyingCleric);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(AmoeboidChangeling.class)
    @DisplayName("A creature that lost its Cleric type does not trigger Orah when it dies")
    void lostClericTypeDoesNotTriggerOrah() {
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new OrahSkyclaveHierophant());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new TaboraxHopesDemise());
        harness.setGraveyard(player1, List.of(new ExpeditionHealer()));

        harness.activateAbility(player1, 0, 1, null, dyingCreature.getId());
        harness.passBothPriorities();
        destroy(dyingCreature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Expedition Healer");
        harness.assertInGraveyard(player1, "Taborax, Hope's Demise");
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
