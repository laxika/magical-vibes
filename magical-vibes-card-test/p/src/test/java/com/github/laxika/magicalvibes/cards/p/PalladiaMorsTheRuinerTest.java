package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.cards.r.RootSnare;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalladiaMorsTheRuiner.class, Shock.class, Disperse.class, GreenwoodSentinel.class, RabidBite.class, RootSnare.class})
class PalladiaMorsTheRuinerTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof before it has dealt damage")
    void hasHexproofBeforeDealingDamage() {
        addReadyPalladia(player1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harness.getPermanentId(player1, "Palladia-Mors, the Ruiner")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Loses hexproof permanently after dealing combat damage")
    void losesHexproofAfterDealingDamage() {
        Permanent palladia = addReadyPalladia(player1);

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, palladia.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shock"));
    }

    @Test
    @DisplayName("Receiving damage does not remove hexproof and its controller can target it")
    void retainsHexproofAfterReceivingDamage() {
        Permanent palladia = addReadyPalladia(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, palladia.getId());

        assertThat(palladia.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, palladia.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Dealing noncombat damage to a creature removes hexproof")
    void losesHexproofAfterNoncombatDamage() {
        Permanent palladia = addReadyPalladia(player1);
        Permanent sentinel = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(palladia.getId(), sentinel.getId()));

        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isFalse();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, palladia.getId());
        assertThat(palladia.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning the same card to the battlefield restores hexproof")
    void regainsHexproofAfterLeavingAndReturning() {
        Permanent palladia = addReadyPalladia(player1);
        declareAttackers(player1, List.of(0));
        resolveCombat(player1);
        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, palladia.getId());
        harness.assertInHand(player1, "Palladia-Mors, the Ruiner");
        harness.assertNotOnBattlefield(player1, "Palladia-Mors, the Ruiner");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, palladia.getCard(), "{3}{R}{G}{W}");
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Palladia-Mors, the Ruiner");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, returned.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Prevented combat damage does not remove hexproof")
    void retainsHexproofWhenCombatDamageIsPrevented() {
        Permanent palladia = addReadyPalladia(player1);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RootSnare()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gqs.hasKeyword(gd, palladia, Keyword.HEXPROOF)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, palladia.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addReadyPalladia(Player player) {
        return addCreatureReady(player, new PalladiaMorsTheRuiner());
    }
}
