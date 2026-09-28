package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatMound.class, Forest.class})
class TheGreatMoundTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new TheGreatMound());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    void createsTappedIndestructibleVibraniumWithRestrictedManaAbility() {
        harness.addToBattlefield(player1, new TheGreatMound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        assertThat(vibranium.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, vibranium, Keyword.INDESTRUCTIBLE)).isTrue();

        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void paysSixManaAndDrawsACard() {
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new TheGreatMound());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }
}
