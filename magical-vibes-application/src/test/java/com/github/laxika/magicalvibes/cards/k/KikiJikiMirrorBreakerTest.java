package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KikiJikiMirrorBreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping creates a hasty token copy scheduled to be sacrificed at the next end step")
    void createsHastyTokenCopySacrificedAtEndStep() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("A creature an opponent controls is not a legal target")
    void cannotCopyOpponentCreature() {
        addKikiJikiReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature you control");
    }

    @Test
    @DisplayName("A legendary creature is not a legal target")
    void cannotCopyLegendaryCreature() {
        Permanent kiki = addKikiJikiReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kiki.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature you control");
    }

    private Permanent addKikiJikiReady(Player player) {
        Permanent perm = new Permanent(new KikiJikiMirrorBreaker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
