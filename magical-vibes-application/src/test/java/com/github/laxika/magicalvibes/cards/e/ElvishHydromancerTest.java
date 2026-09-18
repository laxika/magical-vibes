package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishHydromancer.class, GrizzlyBears.class, FountainOfYouth.class})
class ElvishHydromancerTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotCreateToken() {
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void kickedCreatesTokenCopyOfTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, target));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, target));
    }

    @Test
    void kickedCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ElvishHydromancer()));
        addKickedMana();

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
