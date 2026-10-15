package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinningGlass.class, WoodlandChangeling.class, LowlandOaf.class, Tarfire.class, ThornOfAmethyst.class})
class TwinningGlassTest extends BaseCardTest {

    @Test
    @DisplayName("Offers to cast a hand card sharing a name with a spell cast this turn; accepting casts it for free")
    void castsMatchingSpellFromHandForFree() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling freeCopy = new WoodlandChangeling();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, freeCopy));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0); // cast the first Woodland Changeling
        harness.passBothPriorities();     // resolves onto the battlefield

        harness.activateAbility(player1, 0, null, null); // {1}, {T}
        harness.passBothPriorities();                    // ability resolves

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast the copy for free

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(freeCopy.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(freeCopy.getId()));
    }

    @Test
    @DisplayName("Declining the offer leaves the card in hand and casts nothing")
    void decliningDoesNotCast() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling freeCopy = new WoodlandChangeling();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, freeCopy));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(freeCopy.getId()));
    }

    @Test
    @DisplayName("No offer when no hand card shares a name with a spell cast this turn")
    void noOfferWhenNoHandCardSharesName() {
        WoodlandChangeling prior = new WoodlandChangeling();
        LowlandOaf lowlandOaf = new LowlandOaf(); // different name — not eligible
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, lowlandOaf));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0); // cast Woodland Changeling
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(lowlandOaf.getId()));
    }

    @Test
    @DisplayName("A spell cast by an opponent this turn also enables the ability (any player)")
    void opponentsSpellCounts() {
        Tarfire tarfirePrior = new Tarfire();
        Tarfire tarfireFree = new Tarfire();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(tarfireFree));
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for the {1} ability cost
        harness.setHand(player2, List.of(tarfirePrior));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("No offer when no spell was cast this turn")
    void noOfferWhenNoSpellCastThisTurn() {
        WoodlandChangeling freeCopy = new WoodlandChangeling();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(freeCopy));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(freeCopy.getId()));
    }

    @Test
    void decliningDoesNotPubliclyIdentifyTheHandCard() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling copy = new WoodlandChangeling();
        harness.addToBattlefield(player1, new TwinningGlass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, copy));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int logSize = gd.gameLog.size();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Woodland Changeling");
        assertThat(gd.gameLog.subList(logSize, gd.gameLog.size()).stream()
                .map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("Woodland Changeling"));
    }

    @Test
    void castsOnlyOneMatchingCardPerActivation() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling first = new WoodlandChangeling();
        WoodlandChangeling second = new WoodlandChangeling();
        harness.addToBattlefield(player1, new TwinningGlass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, first, second));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
    }

    @Test
    void canDeclineFirstMatchingCardAndCastAnother() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling first = new WoodlandChangeling();
        WoodlandChangeling second = new WoodlandChangeling();
        harness.addToBattlefield(player1, new TwinningGlass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, first, second));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    void canCastMatchingCreatureDuringOpponentsTurnWhileOriginalIsOnStack() {
        WoodlandChangeling prior = new WoodlandChangeling();
        WoodlandChangeling copy = new WoodlandChangeling();
        harness.addToBattlefield(player1, new TwinningGlass());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(prior));
        harness.setHand(player1, List.of(copy));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(copy.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastMatchingSpellWhenManaTaxCannotBePaid() {
        Tarfire prior = new Tarfire();
        Tarfire copy = new Tarfire();
        harness.addToBattlefield(player1, new TwinningGlass());
        harness.addToBattlefield(player2, new ThornOfAmethyst());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(prior));
        harness.setHand(player1, List.of(copy));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(copy);
    }
}
