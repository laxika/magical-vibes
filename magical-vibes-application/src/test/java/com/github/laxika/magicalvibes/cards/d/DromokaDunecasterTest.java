package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DromokaDunecaster.class, GrizzlyBears.class})
class DromokaDunecasterTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature without flying and taps itself")
    void tapsTargetCreatureWithoutFlying() {
        Permanent dunecaster = addReadyPermanent(player1, new DromokaDunecaster());
        Permanent target = addReadyPermanent(player2, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(dunecaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetCreatureWithFlying() {
        addReadyPermanent(player1, new DromokaDunecaster());
        GrizzlyBears flyingCreature = new GrizzlyBears();
        flyingCreature.setKeywords(Set.of(Keyword.FLYING));
        Permanent target = addReadyPermanent(player2, flyingCreature);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without flying");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
