package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TergridsShadowTest extends BaseCardTest {

    @Test
    void eachPlayerSacrificesTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());

        TergridsShadow shadow = new TergridsShadow();
        harness.setHand(player1, List.of(shadow));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void foretellsAndCastsOnALaterTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        TergridsShadow shadow = new TergridsShadow();
        harness.setHand(player1, List.of(shadow));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(shadow.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castFromExile(player1, shadow.getId());
        harness.passBothPriorities();

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
    }

    private long creatureCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .map(Permanent::getId)
                .count();
    }
}
