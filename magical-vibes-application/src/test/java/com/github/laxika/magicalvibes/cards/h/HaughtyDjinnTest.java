package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({HaughtyDjinn.class, Divination.class, Shock.class, GrizzlyBears.class})
class HaughtyDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of instant and sorcery cards in your graveyard; toughness stays 4")
    void powerCountsOwnInstantAndSorceryCards() {
        Permanent djinn = addDjinnReady(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Instant and sorcery spells you cast cost {1} less")
    void instantAndSorcerySpellsCostOneLess() {
        harness.addToBattlefield(player1, new HaughtyDjinn());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The cost reduction does not affect creatures or opponents' spells")
    void costReductionIsScopedToOwnInstantsAndSorceries() {
        harness.addToBattlefield(player1, new HaughtyDjinn());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDjinnReady(Player player) {
        Permanent permanent = new Permanent(new HaughtyDjinn());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
