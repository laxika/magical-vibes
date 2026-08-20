package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exorcist.class, Gravecrawler.class, GrizzlyBears.class})
class ExorcistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target black creature")
    void destroysBlackCreature() {
        addReadyExorcist(player1);
        Permanent target = addCreatureReady(player2, new Gravecrawler());
        addWhiteAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gravecrawler");
        harness.assertInGraveyard(player2, "Gravecrawler");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        addReadyExorcist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addWhiteAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
    }

    private Permanent addReadyExorcist(Player player) {
        Permanent permanent = new Permanent(new Exorcist());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addWhiteAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
