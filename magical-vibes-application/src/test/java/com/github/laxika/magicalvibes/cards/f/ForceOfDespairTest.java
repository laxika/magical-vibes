package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfDespair.class, GrizzlyBears.class, ScatheZombies.class, DoomBlade.class, HowlingMine.class})
class ForceOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only creatures that entered the battlefield this turn")
    void destroysOnlyCreaturesThatEnteredThisTurn() {
        Permanent olderCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enteredCreature = addCreatureEnteredThisTurn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());

        castNormally();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(olderCreature);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Force of Despair");
        assertThat(enteredCreature).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Can be cast by exiling a black card from hand on an opponent's turn")
    void castsForAlternateCostOnOpponentsTurn() {
        Permanent enteredCreature = addCreatureEnteredThisTurn(player2, new GrizzlyBears());
        DoomBlade blackCard = new DoomBlade();
        harness.setHand(player1, List.of(new ForceOfDespair(), blackCard));
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Force of Despair");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Doom Blade");
        assertThat(enteredCreature).isNotIn(gd.playerBattlefields.get(player2.getId()));
    }

    @Test
    @DisplayName("Cannot use its alternate cost during its controller's turn")
    void alternateCostUnavailableOnOwnTurn() {
        DoomBlade blackCard = new DoomBlade();
        harness.setHand(player1, List.of(new ForceOfDespair(), blackCard));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreatureEnteredThisTurn(Player player, Card creature) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, creature);
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player.getId(), ignored -> new ArrayList<>())
                .add(creature);
        return permanent;
    }

    private void castNormally() {
        harness.setHand(player1, List.of(new ForceOfDespair()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
