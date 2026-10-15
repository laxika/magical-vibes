package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CaveIn;
import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.e.Evermind;
import com.github.laxika.magicalvibes.cards.f.FanningTheFlames;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.m.MantisRider;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.s.SliverQueen;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.cards.y.YawgmothsWill;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamHalls.class, Divination.class, Opt.class, GrizzlyBears.class,
        TormentingVoice.class, Shock.class, MantisRider.class, Evermind.class, YawgmothsWill.class,
        CaveIn.class, FanningTheFlames.class, ManaLeak.class, EnsnaringBridge.class, MycosynthLattice.class, Sift.class,
        SliverQueen.class, SphereOfResistance.class})
class DreamHallsTest extends BaseCardTest {

    @Test
    @DisplayName("A spell can be cast by discarding a card that shares its color")
    void castsByDiscardingSharedColor() {
        harness.addToBattlefield(player1, new DreamHalls());
        Divination spell = new Divination();
        Opt discarded = new Opt();
        harness.setHand(player1, List.of(spell, discarded));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The discard alternative is available to an opponent")
    void opponentCanUseDreamHalls() {
        harness.addToBattlefield(player1, new DreamHalls());
        Divination spell = new Divination();
        Opt discarded = new Opt();
        harness.setHand(player2, List.of(spell, discarded));
        harness.forceActivePlayer(player2);

        harness.castSorceryWithSharedColorDiscard(player2, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("A card with a different color cannot pay for the spell")
    void rejectsDifferentColor() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Divination(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell still pays mana normally when the discard alternative is not selected")
    void canPayManaNormally() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Divination(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The spell's own additional discard cost is paid separately")
    void paysAdditionalDiscardCostSeparately() {
        harness.addToBattlefield(player1, new DreamHalls());
        TormentingVoice spell = new TormentingVoice();
        Shock sharedDiscard = new Shock();
        Shock additionalDiscard = new Shock();
        harness.setHand(player1, List.of(spell, sharedDiscard, additionalDiscard));

        harness.castSorceryWithSharedColorDiscardAndDiscard(player1, 0, 1, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sharedDiscard, additionalDiscard);
    }

    @Test
    @DisplayName("A multicolored spell can use a card sharing either color")
    void multicoloredSpellCanUseEitherSharedColor() {
        harness.addToBattlefield(player1, new DreamHalls());
        MantisRider spell = new MantisRider();
        TormentingVoice discarded = new TormentingVoice();
        harness.setHand(player1, List.of(spell, discarded));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("A colored spell with no mana cost is playable through Dream Halls")
    void noManaCostSpellIsPlayableThroughDreamHalls() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Evermind(), new Opt()));

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("A spell cast from the graveyard can use the discard alternative")
    void graveyardSpellCanUseSharedColorDiscardAlternative() {
        harness.addToBattlefield(player1, new DreamHalls());
        YawgmothsWill will = new YawgmothsWill();
        Divination spell = new Divination();
        Opt discarded = new Opt();
        harness.setHand(player1, List.of(will, discarded));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableFlashbackIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("A spell with its own alternative cost can choose Dream Halls instead")
    void canChooseDreamHallsInsteadOfPrintedAlternativeCost() {
        harness.addToBattlefield(player1, new DreamHalls());
        CaveIn spell = new CaveIn();
        Shock discarded = new Shock();
        harness.setHand(player1, List.of(spell, discarded));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dream Halls does not waive a spell cost increase")
    void cannotSkipSphereOfResistanceCost() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.setHand(player1, List.of(new Sift(), new ManaLeak()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell cost increase is paid along with the Dream Halls discard")
    void paysSphereOfResistanceCost() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.addToBattlefield(player2, new SphereOfResistance());
        Sift spell = new Sift();
        ManaLeak discarded = new ManaLeak();
        harness.setHand(player1, List.of(spell, discarded));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A nonzero X cannot be chosen when paying the Dream Halls alternative")
    void rejectsNonzeroXWithDreamHalls() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new FanningTheFlames(), new Shock()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 5, player2.getId(), null,
                List.of(), List.of(), false, null, null, List.of(), null, List.of(), false,
                null, null, null, null, List.of(), false, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A permitted graveyard spell can actually be cast using Dream Halls")
    void castsPermittedGraveyardSpellWithDreamHalls() {
        harness.addToBattlefield(player1, new DreamHalls());
        Sift spell = new Sift();
        ManaLeak discarded = new ManaLeak();
        harness.setHand(player1, List.of(new YawgmothsWill(), discarded));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), true,
                null, null, List.of(), null, List.of(), false, null, null, null, null,
                List.of(), false, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell, discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Colorless spells cannot use Dream Halls even with a colorless card to discard")
    void rejectsColorlessSpell() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new EnsnaringBridge(), new EnsnaringBridge()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The spell itself cannot be discarded to pay for its cast")
    void rejectsDiscardingTheSpellItself() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Sift()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The discard can precede the spell in hand")
    void discardsCardBeforeSpellInHand() {
        harness.addToBattlefield(player1, new DreamHalls());
        ManaLeak discarded = new ManaLeak();
        Sift spell = new Sift();
        harness.setHand(player1, List.of(discarded, spell));

        harness.castSorceryWithSharedColorDiscard(player1, 1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("A multicolored card can pay for a monocolored spell sharing one color")
    void discardsMulticoloredCardForMonocoloredSpell() {
        harness.addToBattlefield(player1, new DreamHalls());
        Sift spell = new Sift();
        SliverQueen discarded = new SliverQueen();
        harness.setHand(player1, List.of(spell, discarded));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("A colored spell without a mana cost is cast and resolves through Dream Halls")
    void castsAndResolvesSpellWithoutManaCost() {
        harness.addToBattlefield(player1, new DreamHalls());
        Evermind spell = new Evermind();
        ManaLeak discarded = new ManaLeak();
        ManaLeak drawn = new ManaLeak();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, spell);
    }

    @Test
    @DisplayName("Dream Halls uses current colors after Mycosynth Lattice makes all cards colorless")
    void cannotDiscardForColorlessSpellUnderMycosynthLattice() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.setHand(player1, List.of(new Sift(), new ManaLeak()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
