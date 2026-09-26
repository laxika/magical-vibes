package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleaverSkaab.class, ScatheZombies.class})
class CleaverSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two token copies of the sacrificed Zombie")
    void createsTwoTokenCopiesOfSacrificedZombie() {
        Permanent skaab = ready(harness.addToBattlefieldAndReturn(player1, new CleaverSkaab()));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, skaab), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
        harness.assertInGraveyard(player1, "Scathe Zombies");
        assertThat(findPermanents(player1, "Scathe Zombies"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
        assertThat(skaab.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires another Zombie to pay the activation cost")
    void cannotActivateWithoutAnotherZombie() {
        Permanent skaab = ready(harness.addToBattlefieldAndReturn(player1, new CleaverSkaab()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, skaab), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent ready(Permanent permanent) {
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
