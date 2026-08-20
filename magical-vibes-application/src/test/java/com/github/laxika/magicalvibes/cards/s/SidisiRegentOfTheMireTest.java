package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SidisiRegentOfTheMireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and returns a creature with one higher mana value")
    void sacrificesCreatureAndReturnsCreatureWithOneHigherManaValue() {
        Permanent sidisi = addReadySidisi();
        Permanent fodder = addReadyCreature(new GrizzlyBears());
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sidisi);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Knight");
        harness.assertNotInGraveyard(player1, "Benalish Knight");
    }

    @Test
    @DisplayName("Cannot target a creature whose mana value is not one higher")
    void cannotTargetWrongManaValue() {
        addReadySidisi();
        Permanent fodder = addReadyCreature(new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice Sidisi itself")
    void cannotSacrificeSidisiItself() {
        addReadySidisi();
        Card target = new BenalishKnight();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySidisi() {
        return addReadyCreature(new SidisiRegentOfTheMire());
    }

    private Permanent addReadyCreature(Card card) {
        return addCreatureReady(player1, card);
    }
}
