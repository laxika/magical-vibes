package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FelidarCub.class, GloriousAnthem.class, GrizzlyBears.class})
class FelidarCubTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Felidar Cub and destroys target enchantment")
    void destroysTargetEnchantment() {
        addFelidarCub(player1);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Felidar Cub");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can activate with summoning sickness")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new FelidarCub());
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addFelidarCub(player1);
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFelidarCub(Player player) {
        Permanent permanent = new Permanent(new FelidarCub());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addEnchantment(Player player) {
        Permanent permanent = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
