package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.s.SunderingVitae;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkOfEviction.class, BorosRecruit.class, BorosSignet.class, FaithsFetters.class,
        MoldervineCloak.class, LastGasp.class, SunderingVitae.class})
class MarkOfEvictionTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        MarkOfEviction mark = new MarkOfEviction();
        mark.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(mark));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == mark)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        MarkOfEviction mark = new MarkOfEviction();
        mark.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(mark));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returns the enchanted creature and all attached Auras to their owners' hands")
    void returnsEnchantedCreatureAndAllAttachedAuras() {
        BorosRecruit creatureCard = new BorosRecruit();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent mark = addAttachedAura(player1, player1, new MarkOfEviction(), creature);
        Permanent ownAura = addAttachedAura(player1, player1, new FaithsFetters(), creature);
        Permanent opponentAura = addAttachedAura(player2, player1, new MoldervineCloak(), creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(mark.getCard(), ownAura.getCard(), opponentAura.getCard());
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(mark, ownAura);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(creature, opponentAura);
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringItsControllersUpkeep() {
        BorosRecruit creatureCard = new BorosRecruit();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent mark = addAttachedAura(player1, player1, new MarkOfEviction(), creature);

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mark);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Leaves Auras attached to other creatures alone")
    void leavesAurasAttachedToOtherCreaturesAlone() {
        BorosRecruit creatureCard = new BorosRecruit();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        BorosRecruit otherCreatureCard = new BorosRecruit();
        otherCreatureCard.setOwnerId(player2.getId());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, otherCreatureCard);
        Permanent mark = addAttachedAura(player1, player1, new MarkOfEviction(), creature);
        Permanent unrelatedAura = addAttachedAura(player2, player2, new MoldervineCloak(), otherCreature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(mark.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(otherCreature, unrelatedAura)
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Upkeep ability still returns the creature and remaining Auras after Mark is destroyed")
    void returnsCreatureAfterMarkIsDestroyedInResponse() {
        BorosRecruit creatureCard = new BorosRecruit();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent mark = addAttachedAura(player1, player1, new MarkOfEviction(), creature);
        Permanent otherAura = addAttachedAura(player2, player2, new MoldervineCloak(), creature);

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new SunderingVitae()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, mark.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mark.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature, otherAura);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard(), otherAura.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mark.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature, otherAura);
    }

    @Test
    @DisplayName("Does not return the creature or orphaned Auras from the graveyard")
    void doesNotReturnCardsAfterCreatureDiesInResponse() {
        BorosRecruit creatureCard = new BorosRecruit();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Permanent mark = addAttachedAura(player1, player1, new MarkOfEviction(), creature);
        Permanent otherAura = addAttachedAura(player1, player1, new FaithsFetters(), creature);

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new LastGasp()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mark.getCard(), otherAura.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mark.getCard(), otherAura.getCard());
    }

    private Permanent addAttachedAura(Player controller, Player owner, Card auraCard, Permanent creature) {
        auraCard.setOwnerId(owner.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(controller, auraCard);
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
