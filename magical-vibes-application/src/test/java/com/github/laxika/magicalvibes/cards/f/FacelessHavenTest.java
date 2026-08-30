package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FacelessHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Faceless Haven produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent haven = addReadyHaven(player1);

        harness.tapPermanent(player1, indexOf(haven));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation ability requires snow mana")
    void animationRequiresSnowMana() {
        addReadyHaven(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Snow mana animates Faceless Haven as a vigilant 4/3 with all creature types")
    void animatesWithSnowMana() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isTrue();
        assertThat(gqs.getEffectivePower(gd, haven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, haven)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, haven, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.isLand(gd, haven)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent haven = addReadyHaven(player1);
        addSnowMana(player1, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, haven)).isFalse();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, haven, Keyword.CHANGELING)).isFalse();
        assertThat(haven.getCard().getType()).isEqualTo(CardType.LAND);
    }

    private Permanent addReadyHaven(Player player) {
        Permanent haven = new Permanent(new FacelessHaven());
        haven.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(haven);
        return haven;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(ManaColor.COLORLESS, amount);
        pool.addSnowMana(ManaColor.COLORLESS, amount);
    }
}
