package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmedAssailant.class, ParadiseMantle.class})
class ArmedAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and menace while equipped")
    void getsBonusWhileEquipped() {
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());
        Permanent mantle = harness.addToBattlefieldAndReturn(player1, new ParadiseMantle());
        mantle.setAttachedTo(assailant.getId());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not get the bonus while unequipped")
    void doesNotGetBonusWhileUnequipped() {
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new ArmedAssailant());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.MENACE)).isFalse();
    }
}
