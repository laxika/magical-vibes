package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimateNullification.class, ArvadTheCursed.class, GrizzlyBears.class, Island.class, Shock.class})
class UltimateNullificationTest extends BaseCardTest {

    @Test
    void cannotCastWithoutLegendaryCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    void exilesCreaturesAndAllGraveyardsAndPutsSpellOnLibraryBottom() {
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new Shock()));
        prepareCast();

        harness.castSorceryWithSacrifice(player1, 0, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isInstanceOf(UltimateNullification.class);
        harness.assertNotInGraveyard(player1, "Ultimate Nullification");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new UltimateNullification()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
