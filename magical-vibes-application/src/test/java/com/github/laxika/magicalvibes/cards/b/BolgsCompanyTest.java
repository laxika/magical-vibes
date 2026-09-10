package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolgsCompany.class, GoblinPiker.class})
class BolgsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste while controlling another Goblin")
    void hasHasteWithAnotherGoblin() {
        harness.addToBattlefield(player1, new BolgsCompany());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        Permanent company = findPermanent(player1, "Bolg's Company");
        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(goblin);

        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not have haste when alone")
    void doesNotHaveHasteWhenAlone() {
        harness.addToBattlefield(player1, new BolgsCompany());

        Permanent company = findPermanent(player1, "Bolg's Company");
        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Tapping and sacrificing another Goblin adds one black and one red mana")
    void sacrificesAnotherGoblinForMana() {
        Permanent company = addCreatureReady(player1, new BolgsCompany());
        harness.addToBattlefield(player1, new GoblinPiker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(company.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without another Goblin to sacrifice")
    void requiresAnotherGoblin() {
        addCreatureReady(player1, new BolgsCompany());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
