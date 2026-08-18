package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhostLitRaiderTest extends BaseCardTest {

    @Test
    void battlefieldAbilityDealsTwoDamageToTargetCreature() {
        Permanent raider = addCreatureReady(player1, new GhostLitRaider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(raider.isTapped()).isTrue();
    }

    @Test
    void channelDealsFourDamageToTargetCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new GhostLitRaider()));
        harness.addToBattlefield(player2, new SerraAngel());
        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost-Lit Raider");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    void damageAbilitiesCannotTargetPlayers() {
        addCreatureReady(player1, new GhostLitRaider());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new GhostLitRaider()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
