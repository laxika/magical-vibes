package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OmniChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Omni-Changeling keeps changeling when it copies a creature")
    void keepsChangelingWhenCopyingCreature() {
        harness.addToBattlefield(player1, new GoblinKing());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OmniChangeling()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        GameData gd = harness.getGameData();
        Permanent copied = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Omni-Changeling"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, copied, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copied)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copied)).isEqualTo(3);
    }
}
