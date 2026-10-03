package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzebeakForagers.class, Forest.class, GrizzlyBears.class, Ornithopter.class, Unsummon.class})
class BronzebeakForagersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one nonland permanent per opponent until it leaves")
    void etbExilesOneNonlandPermanentPerOpponentUntilItLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        Permanent foragers = castForagers(List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getCardsExiledByPermanent(foragers.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getCard().getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, foragers.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target two nonland permanents controlled by the same opponent")
    void etbTargetsAtMostOnePermanentPerOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForagersCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability puts an exiled card into its owner's graveyard and gains X life")
    void activatedAbilityPutsExiledCardIntoGraveyardAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent foragers = castForagers(List.of(bears.getId()));
        UUID exiledCardId = gd.getCardsExiledByPermanent(foragers.getId()).getFirst().getId();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 2, exiledCardId, Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCardId)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB may decline to exile even when an opponent has a legal target")
    void etbMayChooseNoTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent foragers = harness.enterBattlefieldAndReturn(player1, new BronzebeakForagers());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(foragers.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareForagersCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB cannot target its controller's permanent")
    void etbCannotTargetOwnPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareForagersCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Source leaving before ETB resolves prevents exile")
    void sourceLeavingBeforeEtbResolvesPreventsExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForagersCast();
        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        Permanent foragers = findPermanent(player1, "Bronzebeak Foragers");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, foragers.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bronzebeak Foragers");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Activated ability rejects X different from the exiled card's mana value")
    void activatedAbilityRequiresExactManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent foragers = castForagers(List.of(bears.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 3,
                bears.getCard().getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Activated ability cannot target an unrelated exiled card")
    void activatedAbilityRequiresCardExiledWithThisSource() {
        Permanent foragers = harness.addToBattlefieldAndReturn(player1, new BronzebeakForagers());
        Card bears = new GrizzlyBears();
        harness.setExile(player2, List.of(bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 2,
                bears.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A card put into the graveyard does not return when the source leaves")
    void consumedExiledCardDoesNotReturn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent foragers = castForagers(List.of(bears.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 2,
                bears.getCard().getId(), Zone.EXILE);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, foragers.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No life is gained if the source leaves and the exiled target returns in response")
    void activatedAbilityDoesNotGainLifeWhenTargetLeavesExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent foragers = castForagers(List.of(bears.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 2,
                bears.getCard().getId(), Zone.EXILE);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, foragers.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("X may be zero for an exiled card with mana value zero")
    void activatedAbilityAcceptsZeroManaValue() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent foragers = castForagers(List.of(thopter.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(foragers), 0,
                thopter.getCard().getId(), Zone.EXILE);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.findExiledCard(thopter.getCard().getId())).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Reentering the source does not revive its old entry trigger")
    void reenteringSourceDoesNotReviveOldTrigger() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareForagersCast();
        harness.castCreature(player1, 0, List.of(first.getId()));
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Bronzebeak Foragers");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, original.getId());

        Card returnedCard = gd.playerHands.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of());
        Permanent reentered = harness.enterBattlefieldAndReturn(player1, returnedCard);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        assertThat(gd.getCardsExiledByPermanent(reentered.getId()))
                .extracting(Card::getId)
                .containsExactly(second.getCard().getId());
    }

    private Permanent castForagers(List<UUID> targetIds) {
        prepareForagersCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Bronzebeak Foragers");
    }

    private void prepareForagersCast() {
        harness.setHand(player1, List.of(new BronzebeakForagers()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
