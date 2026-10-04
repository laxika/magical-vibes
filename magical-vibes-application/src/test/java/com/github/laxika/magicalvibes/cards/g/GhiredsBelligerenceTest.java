package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhiredsBelligerence.class, GrizzlyBears.class, ColossalDreadmaw.class, Shock.class})
class GhiredsBelligerenceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals divided damage and populates when any damaged creature dies")
    void dealsDamageAndPopulatesForAnyDamagedCreatureDeath() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBelligerence(Map.of(bears.getId(), 2));

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Populates when a damaged creature dies later in the turn")
    void populatesForLaterDeathThisTurn() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBelligerence(1, Map.of(bears.getId(), 1));

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger for an undamaged creature")
    void doesNotTriggerForUndamagedCreature() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        castBelligerence(Map.of(dreadmaw.getId(), 2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Populates separately for each damaged creature that dies")
    void populatesForEachDeath() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, creatureToken("Soldier Token"));
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBelligerence(4, Map.of(first.getId(), 2, second.getId(), 2));
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, token.getId());
        }
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Populates when the caster's own damaged creature dies")
    void populatesForOwnCreatureDeath() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBelligerence(Map.of(bears.getId(), 2));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Zero X resolves without targets and does not populate")
    void zeroXDoesNotPopulate() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));

        castBelligerence(0, Map.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ghired's Belligerence");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Populate cannot copy an opponent's token or a nontoken creature")
    void populateWithoutControlledCreatureTokenCreatesNothing() {
        harness.addToBattlefield(player2, creatureToken("Soldier Token"));
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBelligerence(Map.of(bears.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevented damage does not qualify a creature for the populate trigger")
    void preventedDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setDamagePreventionShield(1);

        castBelligerence(1, Map.of(bears.getId(), 1));
        assertThat(bears.getMarkedDamage()).isZero();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage assigned to a removed target is not redistributed")
    void removedTargetDoesNotRedistributeDamage() {
        harness.addToBattlefield(player1, creatureToken("Soldier Token"));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new GhiredsBelligerence(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorceryForX(player1, 0, 4, Map.of(bears.getId(), 2, dreadmaw.getId(), 2));
        harness.castAndResolveInstant(player1, 0, bears.getId());

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castBelligerence(Map<java.util.UUID, Integer> assignments) {
        castBelligerence(2, assignments);
    }

    private void castBelligerence(int xValue, Map<java.util.UUID, Integer> assignments) {
        harness.setHand(player1, List.of(new GhiredsBelligerence()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorceryForX(player1, 0, xValue, assignments);
        harness.passBothPriorities();
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
