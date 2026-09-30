package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillToTheBone.class, KrovikanScoundrel.class, BorealGriffin.class, MishrasBauble.class})
class ChillToTheBoneTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonsnow creature")
    void destroysNonsnowCreature() {
        Permanent scoundrel = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, scoundrel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Krovikan Scoundrel");
        harness.assertInGraveyard(player2, "Krovikan Scoundrel");
        harness.assertInGraveyard(player1, "Chill to the Bone");
    }

    @Test
    @DisplayName("Cannot target a snow creature")
    void cannotTargetSnowCreature() {
        harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        Permanent snowGriffin = harness.addToBattlefieldAndReturn(player2, new BorealGriffin());

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, snowGriffin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonsnow creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent bauble = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bauble.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonsnow creature");
    }

    @Test
    @DisplayName("A target that becomes snow before resolution is illegal")
    void targetBecomesSnowBeforeResolution() {
        Permanent scoundrel = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());

        harness.setHand(player1, List.of(new ChillToTheBone()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, scoundrel.getId());
        TestCards.mutableCard(scoundrel).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(scoundrel);
        harness.assertNotInGraveyard(player2, "Krovikan Scoundrel");
        harness.assertInGraveyard(player1, "Chill to the Bone");
    }
}
