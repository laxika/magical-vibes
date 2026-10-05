package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProtectTheNegotiators.class, GrizzlyBears.class})
class ProtectTheNegotiatorsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and creates a Soldier when kicked")
    void kickedSpellCountersAndCreatesSoldier() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castKickedInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).hasSize(1);
    }

    @Test
    @DisplayName("The target spell resolves when its controller pays for each creature")
    void targetSpellResolvesWhenControllerPaysForEachCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castKickedInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Soldier when not kicked")
    void nonKickedSpellDoesNotCreateSoldier() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).isEmpty();
    }

    @Test
    @DisplayName("The newly created Soldier increases the payment before the counter instruction")
    void newlyCreatedSoldierIncreasesPayment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castKickedInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).hasSize(1);
    }

    @Test
    @DisplayName("The Soldier remains when the opponent declines an affordable payment")
    void soldierRemainsWhenPaymentDeclined() {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castKickedInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).hasSize(1);
    }

    @Test
    @DisplayName("With no creatures an unkicked spell allows a zero-mana payment")
    void noCreaturesAllowsZeroManaPayment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(targetSpell.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).isEmpty();
    }

    @Test
    @DisplayName("An illegal spell target prevents the kicked spell from creating a Soldier")
    void illegalTargetPreventsSoldierCreation() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        ProtectTheNegotiators kickedSpell = new ProtectTheNegotiators();
        harness.setHand(player2, List.of(kickedSpell, new ProtectTheNegotiators()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castKickedInstant(player2, 0, targetSpell.getId());
        harness.castAndResolveInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(soldiers(player2)).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(kickedSpell.getId()));
    }

    private List<Permanent> soldiers(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();
    }
}
