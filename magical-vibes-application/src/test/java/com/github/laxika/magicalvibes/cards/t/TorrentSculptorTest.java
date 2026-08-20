package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Lava Axe");
    }

    @Test
    @DisplayName("Flamethrower Sonata does not damage when a non-spell card was discarded")
    void flamethrowerSonataRequiresInstantOrSorceryDiscard() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TorrentSculptor(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Flamethrower Sonata cannot target a permanent controlled by its caster")
    void flamethrowerSonataCannotTargetOwnPermanent() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TorrentSculptor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker you don't control");
    }

    private void castTorrentSculptor() {
        harness.setHand(player1, List.of(new TorrentSculptor()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
