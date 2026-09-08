package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternDismissal.class, GrizzlyBears.class, GloriousAnthem.class, LeoninScimitar.class})
class SternDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Stern Dismissal returns an opponent's creature to its owner's hand")
    void returnsOpponentCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castSternDismissal(target);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Stern Dismissal returns an opponent's enchantment to its owner's hand")
    void returnsOpponentEnchantment() {
        Permanent target = addPermanent(player2, new GloriousAnthem());
        castSternDismissal(target);

        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Stern Dismissal cannot target a creature controlled by its caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareSternDismissal();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment an opponent controls");
    }

    @Test
    @DisplayName("Stern Dismissal cannot target an opponent's artifact")
    void cannotTargetOpponentArtifact() {
        Permanent artifact = addPermanent(player2, new LeoninScimitar());
        addCreatureReady(player2, new GrizzlyBears());
        prepareSternDismissal();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment an opponent controls");
    }

    private void castSternDismissal(Permanent target) {
        prepareSternDismissal();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSternDismissal() {
        harness.setHand(player1, List.of(new SternDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
