package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LorcanWarlockCollector.class, GrizzlyBears.class, Ornithopter.class,
        Shock.class, TomeScour.class, TurnToFrog.class, PlatinumEmperion.class})
class LorcanWarlockCollectorTest extends BaseCardTest {

    private void millCreature(Card creature) {
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature, new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Pays the milled creature's mana value and returns it as a Warlock under the controller's control")
    void returnsMilledCreatureAsWarlock() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 2);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.WARLOCK);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Declining the payment leaves the creature card in its owner's graveyard")
    void decliningPaymentLeavesCardInGraveyard() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Exiles a Warlock you control instead of putting it into a graveyard")
    void exilesControlledWarlockInsteadOfDying() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Does not exile a non-Warlock creature you control instead of dying")
    void doesNotExileNonWarlockCreature() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        harness.addToBattlefield(player1, new Ornithopter());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Ornithopter"));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Ornithopter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card instanceof Ornithopter);
    }

    @Test
    void returnsZeroManaValueCreatureWithoutLosingLife() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        millCreature(new Ornithopter());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, startingLife);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    void cannotReturnCreatureWhenLifePaymentIsUnaffordable() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        harness.setLife(player1, 1);

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnsCreatureThatDiesOnOpponentsBattlefield() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getGrantedSubtypes()).contains(CardSubtype.WARLOCK);
    }

    @Test
    void doesNotTriggerForCreaturePutIntoOwnGraveyard() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void warlockDiesNormallyAfterLorcanLosesAllAbilities() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Lorcan, Warlock Collector"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void canPayZeroLifeWhileLifeTotalCannotChange() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        harness.addToBattlefield(player1, new PlatinumEmperion());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        millCreature(new Ornithopter());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, startingLife);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }
}
