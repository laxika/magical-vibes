package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.t.TasteOfDeath;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrKonradTheGrim.class, GrizzlyBears.class, Shock.class, TomeScour.class,
        Disentomb.class, Reminisce.class, Hushbringer.class, RiseFromTheGrave.class, TasteOfDeath.class})
class SyrKonradTheGrimTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when another creature dies, without double-triggering")
    void damagesOpponentsWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when a creature card enters a graveyard from a non-battlefield zone")
    void damagesOpponentsWhenCreatureCardIsMilled() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage when a creature card leaves your graveyard")
    void damagesOpponentsWhenOwnCreatureCardLeavesGraveyard() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage for each creature card leaving your graveyard at once")
    void damagesOpponentsForEachOwnCreatureCardLeavingGraveyard() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Activated ability mills one card from each player")
    void millsOneCardFromEachPlayer() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        Card player1Card = new Shock();
        Card player2Card = new Shock();
        harness.setLibrary(player1, List.of(player1Card));
        harness.setLibrary(player2, List.of(player2Card));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Card);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Card);
    }

    @Test
    void millingCreaturesFromBothLibrariesTriggersSeparately() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        Card ownCard = new SyrKonradTheGrim();
        Card opposingCard = new SyrKonradTheGrim();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opposingCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void emptyLibraryDoesNotPreventOtherPlayerFromMilling() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        Card opposingCard = new SyrKonradTheGrim();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opposingCard));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCard);
        harness.assertLife(player2, 19);
    }

    @Test
    void doesNotTriggerForOpponentCreatureLeavingGraveyard() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.setGraveyard(player2, List.of(new SyrKonradTheGrim()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerForNoncreatureLeavingOwnGraveyard() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof Shock);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void triggersForOtherCreaturesDyingSimultaneouslyWithSource() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TasteOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Syr Konrad, the Grim");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TasteOfDeath()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Syr Konrad, the Grim");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void hushbringerSuppressesGraveyardLeaveTriggerCausedByReanimation() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.addToBattlefield(player2, new Hushbringer());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void hushbringerDoesNotSuppressGraveyardLeaveTriggerForReturnToHand() {
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.addToBattlefield(player2, new Hushbringer());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    void reanimatingSyrKonradDoesNotTriggerForItsOwnGraveyardDeparture() {
        Card konrad = new SyrKonradTheGrim();
        harness.setGraveyard(player1, List.of(konrad));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, konrad.getId());

        harness.assertOnBattlefield(player1, "Syr Konrad, the Grim");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }
}
