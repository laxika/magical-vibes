package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxbornRollicker.class, FelhideBrawler.class})
class NyxbornRollickerTest extends BaseCardTest {

    @Test
    @DisplayName("Nyxborn Rollicker can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new NyxbornRollicker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rollicker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, rollicker)).isTrue();
    }

    @Test
    @DisplayName("Nyxborn Rollicker can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornRollicker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
    }

    @Test
    @DisplayName("A bestowed Nyxborn Rollicker becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornRollicker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent rollicker = findPermanent(player1, "Nyxborn Rollicker");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rollicker);
        assertThat(gqs.isCreature(gd, rollicker)).isTrue();
        assertThat(rollicker.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature if its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornRollicker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        Permanent rollicker = findPermanent(player1, "Nyxborn Rollicker");
        assertThat(gqs.isCreature(gd, rollicker)).isTrue();
        assertThat(rollicker.isAttached()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof NyxbornRollicker);
    }

    @Test
    @DisplayName("Bestow can enchant and boost an opponent's creature without boosting other creatures")
    void enchantsOpponentsCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new NyxbornRollicker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent rollicker = findPermanent(player1, "Nyxborn Rollicker");
        assertThat(gqs.isCreature(gd, rollicker)).isFalse();
        assertThat(rollicker.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }
}
