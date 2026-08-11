package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AkroanCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Akroan Crusader creates a hasty Soldier token")
    void castingSpellThatTargetsCrusaderCreatesHastySoldier() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID crusaderId = harness.getPermanentId(player1, "Akroan Crusader");
        harness.castInstant(player1, 0, crusaderId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Soldier"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Akroan Crusader")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell that targets Akroan Crusader does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AkroanCrusader());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID crusaderId = harness.getPermanentId(player1, "Akroan Crusader");
        harness.castInstant(player2, 0, crusaderId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
