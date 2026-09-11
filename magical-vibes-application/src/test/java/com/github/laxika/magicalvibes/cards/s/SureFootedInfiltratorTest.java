package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SureFootedInfiltrator.class, DeathcultRogue.class, Forest.class})
class SureFootedInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Rogue makes Sure-Footed Infiltrator unblockable this turn")
    void tappingAnotherRogueMakesItUnblockable() {
        Permanent infiltrator = addReadyCreature(player1, new SureFootedInfiltrator());
        Permanent rogue = addReadyCreature(player1, new DeathcultRogue());

        harness.activateAbility(player1, battlefieldIndex(infiltrator), 0, null, null);
        harness.passBothPriorities();

        assertThat(rogue.isTapped()).isTrue();
        assertThat(infiltrator.isTapped()).isFalse();
        assertThat(infiltrator.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Sure-Footed Infiltrator cannot tap itself for its ability")
    void cannotTapItself() {
        Permanent infiltrator = addReadyCreature(player1, new SureFootedInfiltrator());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(infiltrator), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Combat damage to a player draws a card")
    void drawsOnCombatDamageToPlayer() {
        Permanent infiltrator = addReadyCreature(player1, new SureFootedInfiltrator());
        infiltrator.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
