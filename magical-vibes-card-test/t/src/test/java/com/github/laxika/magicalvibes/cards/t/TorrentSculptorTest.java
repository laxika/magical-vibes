package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpittingEarth;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorrentSculptor.class, Forest.class, GrizzlyBears.class, HillGiant.class, LavaAxe.class, Shock.class, SpittingEarth.class})
class TorrentSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an instant or sorcery and adds half its mana value rounded up")
    void etbExilesCardAndAddsRoundedUpCounters() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setGraveyard(player1, List.of(lavaAxe));
        castTorrentSculptor();

        Permanent sculptor = findPermanent(player1, "Torrent Sculptor");
        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Lava Axe");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lavaAxe);
    }

    @Test
    @DisplayName("ETB lets the controller choose among matching graveyard cards")
    void etbChoosesMatchingCard() {
        LavaAxe lavaAxe = new LavaAxe();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(lavaAxe, shock));
        castTorrentSculptor();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(lavaAxe.getId(), shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(lavaAxe.getId()));
        harness.passBothPriorities();

        Permanent sculptor = findPermanent(player1, "Torrent Sculptor");
        assertThat(sculptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lavaAxe);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Flamethrower Sonata draws after discarding and damages for the discarded card's mana value")
    void flamethrowerSonataDamagesForDiscardedSpellManaValue() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor(), new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Forest");
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Lava Axe");
    }

    @Test
    @DisplayName("Flamethrower Sonata does not damage when a non-spell card was discarded")
    void flamethrowerSonataRequiresInstantOrSorceryDiscard() {
        addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The damage trigger cannot target a permanent controlled by its controller")
    void flamethrowerSonataCannotTargetOwnPermanent() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor(), new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId()).doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void flamethrowerSonataCanLootWithoutOpposingPermanents() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor(), new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Lava Axe");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flamethrowerSonataDrawsWithEmptyHand() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbDoesNotExileCreaturesOrOpponentsCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new LavaAxe()));
        castTorrentSculptor();

        assertThat(findPermanent(player1, "Torrent Sculptor").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lava Axe");
    }

    @Test
    void etbExilesInstantAndRoundsOneManaValueUp() {
        harness.setGraveyard(player1, List.of(new Shock()));
        castTorrentSculptor();

        assertThat(findPermanent(player1, "Torrent Sculptor").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    void decliningWardCountersOpponentsSpell() {
        Permanent sculptor = addCreatureReady(player1, new TorrentSculptor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Torrent Sculptor");
        assertThat(sculptor.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent sculptor = addCreatureReady(player1, new TorrentSculptor());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, sculptor.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Torrent Sculptor");
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent sculptor = addCreatureReady(player1, new TorrentSculptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sculptor.getId());

        harness.assertNotOnBattlefield(player1, "Torrent Sculptor");
    }

    @Test
    void etbHalvesEvenManaValueWithoutRoundingUpFurther() {
        harness.setGraveyard(player1, List.of(new SpittingEarth()));
        castTorrentSculptor();

        assertThat(findPermanent(player1, "Torrent Sculptor").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Spitting Earth");
    }

    @Test
    void etbWithEmptyGraveyardAddsNoCounters() {
        castTorrentSculptor();

        assertThat(findPermanent(player1, "Torrent Sculptor").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void etbStillExilesCardAfterSculptorLeavesBattlefield() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setGraveyard(player1, List.of(lavaAxe));
        harness.setHand(player1, List.of(new TorrentSculptor(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sculptor = findPermanent(player1, "Torrent Sculptor");
        harness.castAndResolveInstant(player1, 0, sculptor.getId());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(lavaAxe.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Torrent Sculptor");
        harness.assertNotInGraveyard(player1, "Lava Axe");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName()).contains("Lava Axe");
    }

    private void castTorrentSculptor() {
        harness.setHand(player1, List.of(new TorrentSculptor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
