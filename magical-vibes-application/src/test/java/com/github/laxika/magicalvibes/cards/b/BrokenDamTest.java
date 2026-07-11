package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrokenDamTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures without horsemanship")
    void tapsTwoTargetCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Permanent> bf = gd.playerBattlefields.get(player2.getId());
        UUID bearsId = bf.get(0).getId();
        UUID spiderId = bf.get(1).getId();

        harness.castSorcery(player1, 0, List.of(bearsId, spiderId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.findPermanentById(gd, bearsId).isTapped()).isTrue();
        assertThat(gqs.findPermanentById(gd, spiderId).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps a single target creature")
    void tapsSingleTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearsId = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        harness.castSorcery(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(harness.getGameData(), bearsId).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with horsemanship")
    void cannotTargetCreatureWithHorsemanship() {
        harness.addToBattlefield(player2, new ShuCavalry());
        harness.setHand(player1, List.of(new BrokenDam()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID horsemanId = gd.playerBattlefields.get(player2.getId()).get(0).getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(horsemanId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
