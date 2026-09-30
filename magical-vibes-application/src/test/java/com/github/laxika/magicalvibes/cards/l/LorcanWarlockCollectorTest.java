package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
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
        Shock.class, TomeScour.class})
class LorcanWarlockCollectorTest extends BaseCardTest {

    private void millCreature(Card creature) {
        creature.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(creature, new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent lorcanReturned() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Pays the milled creature's mana value and returns it as a Warlock under the controller's control")
    void returnsMilledCreatureAsWarlock() {
        harness.addToBattlefield(player1, new LorcanWarlockCollector());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        millCreature(new GrizzlyBears());
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = lorcanReturned();
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
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

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
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Ornithopter);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card instanceof Ornithopter);
    }
}
