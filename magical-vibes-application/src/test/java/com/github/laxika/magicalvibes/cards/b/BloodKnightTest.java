package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodKnight.class, Bandage.class})
class BloodKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Blood Knight has first strike and protection from white")
    void hasFirstStrikeAndProtectionFromWhite() {
        Permanent knight = addCreatureReady(player1, new BloodKnight());

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Blood Knight cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new BloodKnight());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, knight.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }
}
