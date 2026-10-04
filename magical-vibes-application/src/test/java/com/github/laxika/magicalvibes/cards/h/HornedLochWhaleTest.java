package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LagoonBreach;
import com.github.laxika.magicalvibes.cards.m.MerfolkCoralsmith;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornedLochWhale.class, LagoonBreach.class, Island.class,
        MerfolkCoralsmith.class, CandyGrapple.class})
class HornedLochWhaleTest extends BaseCardTest {

    @Test
    void adventurePutsAnOpponentsAttackingCreatureOnTheBottomOfItsOwnersLibrary() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        HornedLochWhale card = new HornedLochWhale();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, attacker.getCard());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotTargetAnAttackingCreatureYouControl() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MerfolkCoralsmith());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void whaleEntersUntappedDuringItsControllersTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castWhale();

        Permanent whale = findPermanent(player1, "Horned Loch-Whale");
        assertThat(whale.isTapped()).isFalse();
    }

    @Test
    void whaleEntersTappedDuringAnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castWhale();

        Permanent whale = findPermanent(player1, "Horned Loch-Whale");
        assertThat(whale.isTapped()).isTrue();
    }

    @Test
    void adventurePutsAnAttackerOnTopAndAllowsCastingTheWhaleFromExile() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        HornedLochWhale card = new HornedLochWhale();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(attacker.getCard(), topCard);
        harness.assertNotOnBattlefield(player2, "Merfolk Coralsmith");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Horned Loch-Whale").isTapped()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCannotTargetANonattackingOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureDoesNotResolveIfTheCreatureStopsAttacking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        HornedLochWhale card = new HornedLochWhale();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Merfolk Coralsmith");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void theOwnerChoosesEvenWhenTheAttackerIsControlledByTheOpponent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MerfolkCoralsmith());
        gd.stolenCreatures.put(attacker.getId(), player1.getId());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player2, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, attacker.getCard());
        harness.assertNotOnBattlefield(player2, "Merfolk Coralsmith");
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyCannotPay() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new HornedLochWhale());
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Candy Grapple");
        assertThat(gqs.getEffectiveToughness(gd, whale)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardAllowsAnOpponentsSpellToResolve() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new HornedLochWhale());
        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, whale)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new HornedLochWhale());
        harness.setHand(player1, List.of(new CandyGrapple()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, whale.getId());

        assertThat(gqs.getEffectiveToughness(gd, whale)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWhale() {
        harness.setHand(player1, List.of(new HornedLochWhale()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
