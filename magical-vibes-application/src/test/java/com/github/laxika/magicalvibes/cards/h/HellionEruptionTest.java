package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellionEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices your creatures and creates one 4/4 Hellion for each")
    void sacrificesCreaturesAndCreatesMatchingHellions() {
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> hellions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(hellions).hasSize(2);
        assertThat(hellions).allSatisfy(hellion -> {
            assertThat(hellion.getEffectivePower()).isEqualTo(4);
            assertThat(hellion.getEffectiveToughness()).isEqualTo(4);
            assertThat(hellion.getCard().getColor()).isEqualTo(CardColor.RED);
        });
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Creates no Hellions when you control no creatures")
    void createsNoHellionsWithoutCreatures() {
        harness.setHand(player1, List.of(new HellionEruption()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
