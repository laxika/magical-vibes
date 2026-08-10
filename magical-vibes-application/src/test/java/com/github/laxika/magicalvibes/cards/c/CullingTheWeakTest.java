package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CullingTheWeakTest extends BaseCardTest {

    @Test
    void castingSacrificesCreatureBeforeResolution() {
        Permanent sacrifice = setupAndCast();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(sacrifice.getOriginalCard());
    }

    @Test
    void resolvingAddsFourBlackMana() {
        setupAndCast();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    @Test
    void spellGoesToGraveyardAfterResolving() {
        Card spell = new CullingTheWeak();
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new CullingTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new CullingTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent setupAndCast() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        harness.setHand(player1, List.of(new CullingTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        return sacrifice;
    }
}
