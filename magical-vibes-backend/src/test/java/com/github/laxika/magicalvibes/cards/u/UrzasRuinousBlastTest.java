package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasRuinousBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles nonlegendary creatures on both sides")
    void exilesNonlegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Nonlegendary creatures should be exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Legendary creatures survive")
    void legendaryCreaturesSurvive() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Arvad the Cursed"));
    }

    @Test
    @DisplayName("Lands survive")
    void landsSurvive() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles nonlegendary enchantments")
    void exilesNonlegendaryEnchantments() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new HonorOfThePure());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Honor of the Pure"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Honor of the Pure"));
    }

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendary() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Urza's Ruinous Blast goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new ArvadTheCursed());

        harness.setHand(player1, List.of(new UrzasRuinousBlast()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Urza's Ruinous Blast"));
    }
}
