package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheImmortalWeapons.class, Divination.class, GiantGrowth.class,
        GrizzlyBears.class, HillGiant.class, Shock.class})
class TheImmortalWeaponsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted instant or sorcery card from the graveyard to hand")
    void etbReturnsInstantOrSorceryToHand() {
        Shock shock = new Shock();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(shock, divination, new GrizzlyBears()));

        castImmortalWeapons();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a noncreature spell gives a chosen creature +2/+0 and menace")
    void noncreatureSpellBoostsAndGrantsMenace() {
        addCreatureReady(player1, new TheImmortalWeapons());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The boost and menace wear off at end of turn")
    void boostAndMenaceWearOffAtEndOfTurn() {
        addCreatureReady(player1, new TheImmortalWeapons());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the boost ability")
    void creatureSpellDoesNotTrigger() {
        addCreatureReady(player1, new TheImmortalWeapons());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB targets only instants and sorceries in its controller's graveyard")
    void etbReturnsInstantAndRestrictsTargets() {
        Shock shock = new Shock();
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(shock, divination, new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        castImmortalWeapons();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactlyInAnyOrder(shock, divination);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.minCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("ETB with no eligible cards does not return a creature or an opponent's instant")
    void etbWithoutLegalTargetsDoesNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        castImmortalWeapons();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "The Immortal Weapons");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot return a targeted card that has left the graveyard")
    void etbTargetLeavingGraveyardIsNotReturned() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castImmortalWeapons();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(shock));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger the boost")
    void opponentSpellDoesNotTrigger() {
        Permanent target = addCreatureReady(player1, new TheImmortalWeapons());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The cast trigger resolves before its spell and may target The Immortal Weapons")
    void triggerResolvesBeforeSpellAndCanTargetSelf() {
        Permanent target = addCreatureReady(player1, new TheImmortalWeapons());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a sorcery triggers the boost before drawing its cards")
    void sorcerySpellTriggersBoost() {
        Permanent target = addCreatureReady(player1, new TheImmortalWeapons());
        Shock firstDraw = new Shock();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Every noncreature spell triggers and the power boosts accumulate")
    void repeatedNoncreatureSpellsAccumulateBoosts() {
        Permanent target = addCreatureReady(player1, new TheImmortalWeapons());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        harness.assertLife(player2, 16);
    }

    private void castImmortalWeapons() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TheImmortalWeapons(), "{4}{R}");
        harness.passBothPriorities();
    }
}
