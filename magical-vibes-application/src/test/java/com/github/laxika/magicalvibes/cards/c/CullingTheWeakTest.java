package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forbid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CullingTheWeak.class, Carnophage.class, Forbid.class})
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
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Carnophage());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void cannotCastWithoutEnoughMana() {
        Card spell = new CullingTheWeak();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Carnophage());
        harness.setHand(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sacrifice.getOriginalCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Carnophage());

        harness.setHand(player1, List.of(new CullingTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    void counteredSpellDoesNotAddMana() {
        Card spell = new CullingTheWeak();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Carnophage());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.passPriority(player1);
        Card counterspell = new Forbid();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, sacrifice.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(counterspell);
    }

    private Permanent setupAndCast() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Carnophage());

        harness.setHand(player1, List.of(new CullingTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        return sacrifice;
    }
}
