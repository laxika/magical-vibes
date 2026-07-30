package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TalrandSkySummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 2/2 flying Drake token")
    void instantCreatesDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
        Permanent drake = findPermanent(player1, "Drake");
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a sorcery creates a Drake token")
    void sorceryCreatesDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Drake token")
    void creatureSpellCreatesNoDrake() {
        harness.addToBattlefield(player1, new TalrandSkySummoner());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drake")).isZero();
    }
}
