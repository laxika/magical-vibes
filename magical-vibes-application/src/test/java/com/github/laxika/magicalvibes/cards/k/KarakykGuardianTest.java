package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyGuardian(Player player) {
        return addCreatureReady(player, new KarakykGuardian());
    }
}
