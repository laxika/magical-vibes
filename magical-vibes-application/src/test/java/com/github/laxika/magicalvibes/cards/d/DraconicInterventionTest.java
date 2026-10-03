package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpellSwindle;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicIntervention.class, DragonEgg.class, GrizzlyBears.class, TormentingVoice.class,
        Shock.class, Cancel.class, SpellSwindle.class})
class DraconicInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the exiled instant or sorcery's mana value")
    void dealsDamageEqualToExiledCardManaValue() {
        TormentingVoice exiledSpell = new TormentingVoice();
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(exiledSpell));
        harness.setHand(player1, List.of(intervention));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, null, 0);

        StackEntry stackEntry = gd.stack.getFirst();
        assertThat(stackEntry.getXValue()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonDragon.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(exiledSpell.getId()))
                .anyMatch(card -> card.getId().equals(intervention.getId()));
    }

    @Test
    @DisplayName("Damages only non-Dragon creatures and exiles creatures killed this turn")
    void sparesDragonsAndExilesKilledCreatures() {
        TormentingVoice exiledSpell = new TormentingVoice();
        Permanent ownNonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonEgg());
        Permanent opponentNonDragon = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(exiledSpell));
        harness.setHand(player1, List.of(intervention));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithGraveyardExile(player1, 0, null, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownNonDragon.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dragon.getId()));
        assertThat(dragon.getMarkedDamage()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentNonDragon.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentNonDragon.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot exile a creature card to pay the additional cost")
    void rejectsCreatureAsAdditionalCost() {
        GrizzlyBears creature = new GrizzlyBears();
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(intervention));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).contains(intervention);
    }

    @Test
    @DisplayName("Cannot cast without an instant or sorcery in the caster's graveyard")
    void cannotUseOpponentGraveyardForCost() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new DraconicIntervention()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("An instant can pay the cost, and a surviving damaged creature is exiled if killed later")
    void exilesSurvivorKilledLaterThisTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new DraconicIntervention(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstantWithGraveyardExile(player1, 0, null, 0);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("The exile replacement ends at the end of the turn")
    void survivorDiesNormallyNextTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new DraconicIntervention()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstantWithGraveyardExile(player1, 0, null, 0);
        harness.passBothPriorities();
        assertThat(creature.getMarkedDamage()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature.getCard());
    }

    @Test
    @DisplayName("Countering leaves the cost exiled but puts Intervention into the graveyard without damage")
    void counteredSpellDoesNotExileItselfOrDealDamage() {
        Shock cost = new Shock();
        DraconicIntervention intervention = new DraconicIntervention();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(cost));
        harness.setHand(player1, List.of(intervention));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstantWithGraveyardExile(player1, 0, null, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cost);
        harness.castInstant(player2, 0, intervention.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Draconic Intervention");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cost).doesNotContain(intervention);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The exiled card's mana value does not increase Intervention's mana value on the stack")
    void manaValueRemainsFourOnStack() {
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(new TormentingVoice()));
        harness.setHand(player1, List.of(intervention));
        harness.setHand(player2, List.of(new SpellSwindle()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstantWithGraveyardExile(player1, 0, null, 0);
        harness.castInstant(player2, 0, intervention.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Draconic Intervention");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Treasure"))
                .hasSize(4);
    }
}
