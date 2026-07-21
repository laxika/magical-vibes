package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GraspingDunes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IpnuRivuletTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyRivulet(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {U} produces blue and costs 1 life")
    void tapPayLifeForBlue() {
        addReadyRivulet(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mill ability sacrifices a Desert and mills target player four cards")
    void millSacrificesDesert() {
        Permanent rivulet = addReadyRivulet(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(rivulet.getId()));
        harness.assertInGraveyard(player1, "Ipnu Rivulet");
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent rivulet = addReadyRivulet(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(rivulet.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Can target yourself to mill")
    void canTargetSelf() {
        addReadyRivulet(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 4);
        // Ipnu Rivulet itself is also in the graveyard (sacrificed).
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Mill ability can be activated at instant speed")
    void millIsInstantSpeed() {
        addReadyRivulet(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
    }

    private Permanent addReadyRivulet(Player player) {
        Permanent perm = new Permanent(new IpnuRivulet());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
