package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingLectern.class, GrizzlyBears.class, Forest.class})
class LivingLecternTest extends BaseCardTest {

    @Test
    void sacrificesDrawsAndCreatesSorcererRoleAttachedToAnotherCreature() {
        Permanent lectern = harness.addToBattlefieldAndReturn(player1, new LivingLectern());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Living Lectern");
        harness.assertInGraveyard(player1, "Living Lectern");
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void canChooseNoTargetAndStillDraws() {
        harness.addToBattlefield(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
    }

    @Test
    void cannotTargetOpponentCreatureOrTheLecternItself() {
        Permanent lectern = harness.addToBattlefieldAndReturn(player1, new LivingLectern());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lectern.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Living Lectern");
    }
}
