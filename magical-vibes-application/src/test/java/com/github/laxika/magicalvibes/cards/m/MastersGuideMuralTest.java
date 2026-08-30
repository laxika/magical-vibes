package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MastersGuideMural.class, MastersManufactory.class, DarksteelRelic.class})
class MastersGuideMuralTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by creating a 4/4 white and blue Golem artifact creature token")
    void entersByCreatingGolemToken() {
        harness.setHand(player1, List.of(new MastersGuideMural()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent golem = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(golem.getCard().getName()).isEqualTo("Golem");
        assertThat(golem.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(golem.getCard().getSubtypes()).containsExactly(CardSubtype.GOLEM);
        assertThat(golem.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golem.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golem.getEffectivePower()).isEqualTo(4);
        assertThat(golem.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Craft returns Master's Manufactory transformed and it creates a Golem")
    void craftsIntoManufactoryAndCreatesGolem() {
        Permanent mural = harness.addToBattlefieldAndReturn(player1, new MastersGuideMural());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(mural), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent manufactory = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MastersManufactory)
                .findFirst()
                .orElseThrow();
        assertThat(manufactory.isTransformed()).isTrue();
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();

        int golemCountBefore = countGolems();
        harness.activateAbility(player1, battlefieldIndex(manufactory), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countGolems()).isEqualTo(golemCountBefore + 1);
    }

    private int countGolems() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .count();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
