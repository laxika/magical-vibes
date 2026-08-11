package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlenElendrasAnswerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters every opponent spell and creates one Faerie per countered spell")
    void countersOpponentSpellsAndCreatesFaeries() {
        GrizzlyBears ownSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(ownSpell, new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);

        LightningBolt firstOpponentSpell = new LightningBolt();
        LightningBolt secondOpponentSpell = new LightningBolt();
        harness.setHand(player2, List.of(firstOpponentSpell, secondOpponentSpell));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Lightning Bolt", "Lightning Bolt");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownSpell.getId()));
        assertThat(faerieTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Counters opponent activated abilities and creates a Faerie")
    void countersOpponentAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null,
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(faerieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not count an uncounterable spell")
    void doesNotCountUncounterableSpell() {
        harness.setHand(player2, List.of(new VolcanicFallout()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0);

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(faerieTokens(player1)).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private List<Permanent> faerieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Faerie"))
                .toList();
    }
}
