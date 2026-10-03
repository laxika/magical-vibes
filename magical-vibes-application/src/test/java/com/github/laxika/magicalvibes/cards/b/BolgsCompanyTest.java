package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinTownFlunkies;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolgsCompany.class, GoblinTownFlunkies.class, BoggartShenanigans.class})
class BolgsCompanyTest extends BaseCardTest {

    @Test
    @DisplayName("Has haste while controlling another Goblin")
    void hasHasteWithAnotherGoblin() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new BolgsCompany());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinTownFlunkies());

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
        harness.addToBattlefield(player1, new GoblinTownFlunkies());

        harness.activateAbility(player1, 0, null, null);

        assertThat(company.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Goblin-town Flunkies");
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

    @Test
    @DisplayName("An opponent's Goblin grants neither haste nor a sacrifice payment")
    void opponentsGoblinDoesNotQualify() {
        Permanent company = addCreatureReady(player1, new BolgsCompany());
        harness.addToBattlefield(player2, new GoblinTownFlunkies());

        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(company.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Goblin-town Flunkies");
    }

    @Test
    @DisplayName("Conditional haste allows activation immediately, even when sacrificing the last other Goblin")
    void hasteAllowsActivationBeforeSacrificingLastGoblin() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new BolgsCompany());
        company.setSummoningSick(true);
        harness.addToBattlefield(player1, new GoblinTownFlunkies());

        harness.activateAbility(player1, 0, null, null);

        assertThat(company.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Goblin-town Flunkies");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Company cannot activate even with another Goblin")
    void tappedCompanyCannotActivate() {
        Permanent company = addCreatureReady(player1, new BolgsCompany());
        company.setTapped(true);
        harness.addToBattlefield(player1, new GoblinTownFlunkies());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Goblin-town Flunkies");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A noncreature Goblin grants haste and can be sacrificed for mana")
    void canSacrificeKindredGoblinEnchantment() {
        Permanent company = harness.addToBattlefieldAndReturn(player1, new BolgsCompany());
        company.setSummoningSick(true);
        harness.addToBattlefield(player1, new BoggartShenanigans());

        assertThat(gqs.hasKeyword(gd, company, Keyword.HASTE)).isTrue();
        harness.activateAbility(player1, 0, null, null);

        assertThat(company.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
