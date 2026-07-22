package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodPetalCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike while attacking")
    void hasFirstStrikeWhileAttacking() {
        Permanent celebrant = addCreatureReady(player1, new BloodPetalCelebrant());

        declareAttackers(player1, List.of(0));

        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("No first strike when not attacking")
    void noFirstStrikeWhenNotAttacking() {
        Permanent celebrant = addCreatureReady(player1, new BloodPetalCelebrant());

        assertThat(gqs.hasKeyword(gd, celebrant, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("When Blood Petal Celebrant dies, a Blood token is created")
    void deathCreatesBloodToken() {
        harness.addToBattlefield(player1, new BloodPetalCelebrant());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        List<Permanent> bloods = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .toList();
        assertThat(bloods).hasSize(1);
        Permanent blood = bloods.getFirst();
        assertThat(blood.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(blood.getCard().getSubtypes()).contains(CardSubtype.BLOOD);
        assertThat(blood.getCard().isToken()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blood Petal Celebrant"));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int idx : attackerIndices) {
            battlefield.get(idx).setAttacking(true);
        }
    }
}
