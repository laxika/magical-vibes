package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.BarbedSextant;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.o.OrcishCannoneers;
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

@CardUsed({BrownOuphe.class, BarbedSextant.class, IcyManipulator.class,
        Incinerate.class, OrcishCannoneers.class})
class BrownOupheTest extends BaseCardTest {

    /** The Ouphe's ability needs {T}, so it must have been under its controller's control since their turn began. */
    private void addReadyOuphe(Player player) {
        addCreatureReady(player, new BrownOuphe());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Counters an activated ability from an artifact source")
    void countersArtifactActivatedAbility() {
        addReadyOuphe(player1);

        Permanent target = addCreatureReady(player1, new OrcishCannoneers());
        IcyManipulator icy = new IcyManipulator();
        harness.addToBattlefield(player2, icy);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, icy.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability from an artifact source")
    void cannotCounterManaAbility() {
        addReadyOuphe(player1);

        BarbedSextant sextant = new BarbedSextant();
        harness.addToBattlefield(player2, sextant);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        assertThat(harness.getGameData().stack).isEmpty();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sextant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotCounterNonArtifactAbility() {
        addReadyOuphe(player1);

        Permanent cannoneers = addCreatureReady(player2, new OrcishCannoneers());
        int player1LifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int player2LifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cannoneers.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player1, player1LifeBefore - 2);
        harness.assertLife(player2, player2LifeBefore - 3);
    }

    @Test
    @DisplayName("Cannot target a spell on the stack")
    void cannotCounterSpell() {
        addReadyOuphe(player1);

        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        int player1LifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, incinerate.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player1, player1LifeBefore - 3);
    }

    @Test
    @CardUsed(LiquimetalCoating.class)
    @DisplayName("Counters an ability after its non-artifact source becomes an artifact")
    void countersAbilityAfterSourceBecomesArtifact() {
        addReadyOuphe(player1);
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent cannoneers = addCreatureReady(player2, new OrcishCannoneers());
        int player1LifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int player2LifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 1, null, cannoneers.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(cannoneers)).isTrue();
        harness.activateAbility(player1, 0, null, cannoneers.getCard().getId());
        harness.passBothPriorities();

        harness.assertLife(player1, player1LifeBefore);
        harness.assertLife(player2, player2LifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
