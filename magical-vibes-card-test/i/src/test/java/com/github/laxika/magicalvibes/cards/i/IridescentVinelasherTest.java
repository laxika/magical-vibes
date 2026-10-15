package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IridescentVinelasher.class, Forest.class})
class IridescentVinelasherTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new IridescentVinelasher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void doesNotCreateOffspringTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new IridescentVinelasher()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void landfallDealsOneDamageToTargetOpponent() {
        harness.addToBattlefield(player1, new IridescentVinelasher());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void offspringCopyAlsoTriggersLandfallAndOnlyTargetsOpponent() {
        harness.setHand(player1, List.of(new IridescentVinelasher(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.playLand(player1, 0);

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                    .containsExactly(player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new IridescentVinelasher());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void eachLandEnteringTriggersEvenWhenNotPlayedFromHand() {
        harness.addToBattlefield(player1, new IridescentVinelasher());
        harness.setLife(player2, 20);

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new Forest());
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.assertLife(player2, 19 - i);
        }
    }
}
