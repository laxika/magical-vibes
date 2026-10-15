package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterSoldierIcyAssassin.class, LeoninScimitar.class, GrizzlyBears.class})
class WinterSoldierIcyAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter and can attach one Equipment")
    void returnsWithFinalityCounterAndAttachesEquipment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(winterSoldier.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
    }

    @Test
    @DisplayName("Choosing among multiple Equipment attaches only the chosen one")
    void choosesOneEquipment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent returned = findPermanentByCardId(winterSoldier.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(second.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(first.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A finality counter exiles Winter Soldier instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent winterSoldier = harness.addToBattlefieldAndReturn(player1, new WinterSoldierIcyAssassin());
        winterSoldier.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, winterSoldier));

        harness.assertNotOnBattlefield(player1, "Winter Soldier, Icy Assassin");
        harness.assertNotInGraveyard(player1, "Winter Soldier, Icy Assassin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(winterSoldier.getCard().getId()));
    }

    @Test
    @DisplayName("Attachment can be declined without preventing the return")
    void canDeclineAttachment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent returned = findPermanentByCardId(winterSoldier.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returning cannot attach an opponent's Equipment")
    void cannotAttachOpponentsEquipment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanentByCardId(winterSoldier.getId()).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The power bonus scales with attached Equipment and updates when one moves away")
    void countsEachAttachedEquipment() {
        Permanent winterSoldier = harness.addToBattlefieldAndReturn(player1, new WinterSoldierIcyAssassin());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, winterSoldier.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 2, null, winterSoldier.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, winterSoldier)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, winterSoldier)).isEqualTo(4);

        harness.activateAbility(player1, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(first.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, winterSoldier)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, winterSoldier)).isEqualTo(3);
    }

    @Test
    @DisplayName("The graveyard ability returns only its own card, not another copy")
    void returnsOnlyTheActivatedCopy() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        Card otherCopy = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier, otherCopy));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(findPermanentByCardId(winterSoldier.getId()).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCopy);
    }

    @Test
    @DisplayName("An activation that did not return Winter Soldier cannot attach Equipment to a previously returned object")
    void earlierActivationCannotAttachAfterAnotherActivationReturnsIt() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanentByCardId(winterSoldier.getId()).getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
