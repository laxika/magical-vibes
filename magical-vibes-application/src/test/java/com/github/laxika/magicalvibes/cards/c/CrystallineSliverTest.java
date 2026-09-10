package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AcidicSliver;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
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

@CardUsed({CrystallineSliver.class, SpinedSliver.class, YouthfulKnight.class,
        Shock.class, AcidicSliver.class})
class CrystallineSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers have shroud, including Crystalline Sliver and opposing Slivers")
    void grantsShroudToAllSlivers() {
        Permanent crystallineSliver = addCreatureReady(player1, new CrystallineSliver());
        Permanent ownSliver = addCreatureReady(player1, new SpinedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SpinedSliver());
        Permanent nonSliver = addCreatureReady(player1, new YouthfulKnight());

        assertThat(gqs.hasKeyword(gd, crystallineSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Slivers lose granted shroud when Crystalline Sliver leaves the battlefield")
    void grantedShroudIsRemovedWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new CrystallineSliver());
        Permanent sliver = addCreatureReady(player1, new SpinedSliver());
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents spells from targeting own and opposing Slivers")
    void shroudPreventsSpellTargetingSlivers() {
        Permanent ownSliver = addCreatureReady(player1, new CrystallineSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SpinedSliver());

        assertThatShockCannotTarget(ownSliver);
        assertThatShockCannotTarget(opposingSliver);
    }

    @Test
    @DisplayName("Shroud prevents activated abilities from targeting Slivers")
    void shroudPreventsAbilityTargetingSlivers() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent sliver = addCreatureReady(player1, new CrystallineSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, sliver.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud does not prevent spells from targeting non-Sliver creatures")
    void nonSliversRemainTargetable() {
        Permanent nonSliver = addCreatureReady(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, nonSliver.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        harness.assertInGraveyard(player2, "Youthful Knight");
    }

    private void assertThatShockCannotTarget(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
