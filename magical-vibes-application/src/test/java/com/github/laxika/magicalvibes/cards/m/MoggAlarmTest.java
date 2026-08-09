package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoggAlarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 1/1 red Goblin tokens when cast for mana")
    void createsTwoGoblinTokens() {
        harness.setHand(player1, List.of(new MoggAlarm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> goblins = findGoblinTokens();
        assertThat(goblins).hasSize(2);
        for (Permanent goblin : goblins) {
            assertThat(goblin.getCard().getPower()).isEqualTo(1);
            assertThat(goblin.getCard().getToughness()).isEqualTo(1);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Can be cast by sacrificing two Mountains instead of paying mana")
    void castsBySacrificingTwoMountains() {
        UUID mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        UUID mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new MoggAlarm()));

        harness.castWithAlternateCost(player1, 0, List.of(mountain1, mountain2));
        harness.passBothPriorities();

        assertThat(findGoblinTokens()).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mogg Alarm");
    }

    @Test
    @DisplayName("Alternate cost requires two Mountains")
    void alternateCostRequiresTwoMountains() {
        UUID mountain = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(mountain)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost rejects a non-Mountain")
    void alternateCostRejectsNonMountain() {
        Permanent island1 = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent island2 = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new MoggAlarm()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(
                player1, 0, List.of(island1.getId(), island2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> findGoblinTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Goblin"))
                .toList();
    }
}
