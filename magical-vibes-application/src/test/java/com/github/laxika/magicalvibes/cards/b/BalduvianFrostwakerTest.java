package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalduvianFrostwakerTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target snow land into a permanent 2/2 blue Elemental with flying")
    void animatesTargetSnowLand() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, snowLand)).containsExactly(CardColor.BLUE);
        assertThat(snowLand.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.FLYING)).isTrue();
        assertThat(snowLand.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Permanent animation survives end-of-turn cleanup")
    void animationSurvivesCleanup() {
        addReadyFrostwaker(player1);
        Permanent snowLand = addSnowLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, snowLand.getId());
        harness.passBothPriorities();
        snowLand.resetModifiers();

        assertThat(gqs.isCreature(gd, snowLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snowLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, snowLand)).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, snowLand, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonsnow land")
    void cannotTargetNonsnowLand() {
        addReadyFrostwaker(player1);
        Permanent plains = new Permanent(new Plains());
        gd.playerBattlefields.get(player1.getId()).add(plains);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFrostwaker(Player player) {
        Permanent permanent = new Permanent(new BalduvianFrostwaker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addSnowLand(Player player) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
        return snowLand;
    }
}
