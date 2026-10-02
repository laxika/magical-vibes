package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BubbleUp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SleightOfHand;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquaticAlchemistBubbleUp.class, BubbleUp.class, GrizzlyBears.class, Shock.class, SleightOfHand.class})
class AquaticAlchemistBubbleUpTest extends BaseCardTest {

    @Test
    void adventurePutsTargetInstantOrSorceryOnTopOfLibraryAndExilesCard() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new Shock();
        Card oldTop = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, oldTop);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetCreatureCard() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsOnlyTheFirstInstantOrSorcerySpellEachTurn() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isZero();
    }

    @Test
    void creatureSpellDoesNotConsumeTheFirstInstantOrSorceryTrigger() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);

        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void doesNotTriggerIfAnInstantWasCastBeforeItEnteredThisTurn() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(alchemist.getPowerModifier()).isZero();
    }

    @Test
    void adventureCountsAsTheFirstSorceryBeforeTheCreatureIsCastFromExile() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new Shock();
        harness.setHand(player1, List.of(card, new Shock()));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void adventureCannotTargetAnOpponentsInstant() {
        Card target = new Shock();
        harness.setHand(player1, List.of(new AquaticAlchemistBubbleUp()));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureCannotTargetAnAdventurerInTheGraveyard() {
        Card target = new AquaticAlchemistBubbleUp();
        harness.setHand(player1, List.of(new AquaticAlchemistBubbleUp()));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureWithAnIllegalTargetGoesToGraveyardInsteadOfExile() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new Shock();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void anOpponentsInstantDoesNotConsumeTheControllersFirstSpellTrigger() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void aSorceryAdventureTriggersTheCreatureAndReturnsASorceryCard() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        Card target = new SleightOfHand();
        harness.setHand(player1, List.of(new AquaticAlchemistBubbleUp()));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void triggersAgainOnTheControllersFirstInstantDuringTheNextPlayersTurn() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(alchemist.getPowerModifier()).isZero();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
    }
}
