package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigardaHostOfHeronsTest extends BaseCardTest {

    private long creatureCount(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }

    private void castBarterInBlood() {
        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's targeted edict can't make Sigarda's controller sacrifice")
    void opponentTargetedEdictDoesNothing() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
    }

    @Test
    @DisplayName("An opponent's each-player edict skips Sigarda's controller but still hits the caster")
    void opponentEachPlayerEdictSkipsProtectedPlayer() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castBarterInBlood();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isZero();
        // Sigarda plus both Bears are untouched
        assertThat(creatureCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sigarda doesn't stop her controller's own sacrifice effects")
    void ownEffectStillCausesSacrifice() {
        harness.addToBattlefield(player1, new SigardaHostOfHerons());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castBarterInBlood();

        // Player1 has three creatures and must choose two of them to sacrifice
        assertThat(harness.getGameData().interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Protection is lost once Sigarda leaves the battlefield")
    void protectionEndsWhenSigardaLeaves() {
        Permanent sigarda = new Permanent(new SigardaHostOfHerons());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(sigarda);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.getGameData().playerBattlefields.get(player2.getId()).remove(sigarda);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
