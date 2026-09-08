package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrainInAJarTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a charge counter and offers a matching instant or sorcery for free")
    void addsCounterAndCastsMatchingSpell() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Opt");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(opt.getId());
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(opt.getId()));
    }

    @Test
    @DisplayName("Offers no spell whose mana value differs from the charge-counter count")
    void requiresExactManaValue() {
        harness.addToBattlefield(player1, new BrainInAJar());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(counsel);
    }

    @Test
    @DisplayName("Removes X charge counters and scries X")
    void removesCountersAndScriesX() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new BrainInAJar());
        jar.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, 2, null);
        harness.passBothPriorities();

        assertThat(jar.getCounterCount(CounterType.CHARGE)).isZero();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }
}
