package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ErraticPortal;
import com.github.laxika.magicalvibes.cards.s.Slaughter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mirozel.class, Slaughter.class, ErraticPortal.class})
class MirozelTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by a spell")
    void returnsToHandWhenTargetedBySpell() {
        Permanent miroz = harness.addToBattlefieldAndReturn(player1, new Mirozel());

        harness.setHand(player2, List.of(new Slaughter()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, miroz.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mirozel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(miroz.getId()));
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by an ability")
    void returnsToHandWhenTargetedByAbility() {
        Permanent miroz = harness.addToBattlefieldAndReturn(player1, new Mirozel());

        Permanent portal = harness.addToBattlefieldAndReturn(player2, new ErraticPortal());
        portal.setSummoningSick(false);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID mirozId = miroz.getId();
        int portalIndex = gd.playerBattlefields.get(player2.getId()).indexOf(portal);
        harness.activateAbility(player2, portalIndex, null, mirozId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mirozel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(mirozId));
    }

    @Test
    @DisplayName("Does not return when another creature becomes the target of a spell")
    void staysOnBattlefieldWhenAnotherCreatureIsTargeted() {
        Permanent unaffectedMirozel = harness.addToBattlefieldAndReturn(player1, new Mirozel());
        Permanent targetedMirozel = harness.addToBattlefieldAndReturn(player1, new Mirozel());
        UUID unaffectedMirozelId = unaffectedMirozel.getId();
        UUID targetedMirozelId = targetedMirozel.getId();

        harness.setHand(player2, List.of(new Slaughter()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, targetedMirozelId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(unaffectedMirozelId))
                .noneMatch(permanent -> permanent.getId().equals(targetedMirozelId));
    }
}
