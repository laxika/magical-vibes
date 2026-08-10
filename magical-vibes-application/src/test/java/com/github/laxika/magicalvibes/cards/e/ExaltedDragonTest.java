package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExaltedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted Dragon can't attack without a land to sacrifice")
    void cannotAttackWithoutLandToSacrifice() {
        addCreatureReady(player1, new ExaltedDragon());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with Exalted Dragon sacrifices a land")
    void attackingSacrificesALand() {
        addCreatureReady(player1, new ExaltedDragon());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(landsOnBattlefield()).hasSize(1);
    }

    private List<Permanent> landsOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .toList();
    }
}
