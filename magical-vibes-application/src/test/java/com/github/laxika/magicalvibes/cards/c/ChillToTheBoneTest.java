package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChillToTheBoneTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonsnow creature")
    void destroysNonsnowCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chill to the Bone");
    }

    @Test
    @DisplayName("Cannot target a snow creature")
    void cannotTargetSnowCreature() {
        Permanent nonsnowBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(nonsnowBears);

        Permanent snowBears = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(snowBears).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        gd.playerBattlefields.get(player2.getId()).add(snowBears);

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, snowBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonsnow creature");
    }

    @Test
    @DisplayName("A target that becomes snow before resolution is illegal")
    void targetBecomesSnowBeforeResolution() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bears.getId());
        TestCards.mutableCard(bears).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chill to the Bone");
    }
}
