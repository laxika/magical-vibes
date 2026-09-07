package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({SyrKonradTheGrim.class, GrizzlyBears.class, Shock.class, TomeScour.class, Disentomb.class, Reminisce.class})
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

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
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
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
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
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
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
}
