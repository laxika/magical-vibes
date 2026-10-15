package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DragonclawStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StormscaleScion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarakykGuardian.class, Shock.class, DragonclawStrike.class, StormscaleScion.class})
class KarakykGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof before it has dealt damage")
    void hasHexproofBeforeDealingDamage() {
        addReadyGuardian(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Karakyk Guardian")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Loses hexproof permanently after dealing combat damage")
    void losesHexproofAfterDealingDamage() {
        Permanent guardian = addReadyGuardian(player1);

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, guardian.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Its controller can target it and damage received does not remove hexproof")
    void retainsHexproofAfterReceivingDamage() {
        Permanent guardian = addReadyGuardian(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guardian.getId());

        assertThat(guardian.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, guardian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Hexproof does not return on a later turn after dealing damage")
    void damageHistoryPersistsAcrossTurns() {
        Permanent guardian = addReadyGuardian(player1);
        declareAttackers(player1, List.of(0));
        resolveCombat(player1);
        harness.assertLife(player2, 14);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Dealing noncombat damage in a fight removes hexproof")
    void losesHexproofAfterFightDamage() {
        Permanent guardian = addReadyGuardian(player1);
        harness.addToBattlefield(player2, new StormscaleScion());
        harness.setHand(player1, List.of(new DragonclawStrike()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(guardian.getId(),
                harness.getPermanentId(player2, "Stormscale Scion")));

        harness.assertInGraveyard(player2, "Stormscale Scion");
        assertThat(guardian.getMarkedDamage()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Damage dealt by one Guardian does not remove another Guardian's hexproof")
    void damageHistoryIsPerPermanent() {
        Permanent attacker = addReadyGuardian(player1);
        Permanent otherGuardian = addReadyGuardian(player1);

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        harness.assertLife(player2, 14);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherGuardian, Keyword.HEXPROOF)).isTrue();
    }

    private Permanent addReadyGuardian(Player player) {
        return addCreatureReady(player, new KarakykGuardian());
    }
}
