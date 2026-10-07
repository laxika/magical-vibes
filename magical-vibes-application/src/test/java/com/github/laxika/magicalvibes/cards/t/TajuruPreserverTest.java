package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UlamogsCrusher;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruPreserver.class, BarterInBlood.class, CruelEdict.class, GrizzlyBears.class,
        Forest.class, UlamogsCrusher.class})
class TajuruPreserverTest extends BaseCardTest {

    private long creatureCount(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }

    @Test
    @DisplayName("An opponent's targeted edict can't make Tajuru Preserver's controller sacrifice")
    void opponentTargetedEdictDoesNothing() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
    }

    @Test
    @DisplayName("An opponent's each-player edict skips Tajuru Preserver's controller")
    void opponentEachPlayerEdictSkipsProtectedPlayer() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tajuru Preserver protects itself when it is the only creature")
    void onlyPreserverIsProtected() {
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
    }

    @Test
    @DisplayName("Tajuru Preserver does not protect its controller's opponent")
    void opponentIsNotProtected() {
        harness.addToBattlefield(player1, new TajuruPreserver());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tajuru Preserver");
    }

    @Test
    @DisplayName("The controller's own spell can sacrifice Tajuru Preserver and another creature")
    void ownSpellStillCausesSacrifice() {
        harness.addToBattlefield(player1, new TajuruPreserver());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tajuru Preserver");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's annihilator ability cannot sacrifice creatures or lands")
    void annihilatorCannotCauseSacrifice() {
        addCreatureReady(player1, new UlamogsCrusher());
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new Forest());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Tajuru Preserver");
        harness.assertOnBattlefield(player2, "Forest");
    }
}
