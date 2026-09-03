package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireDiamond.class})
class FireDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new FireDiamond()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent diamond = findPermanent(player1, "Fire Diamond");
        assertThat(diamond.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for mana adds red mana")
    void tapForRedMana() {
        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        diamond.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isGreaterThanOrEqualTo(1);
        assertThat(diamond.isTapped()).isTrue();
    }

}
