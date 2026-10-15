package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InkshapeDemonstrator.class, HillGiant.class, Shock.class, Blaze.class})
class InkshapeDemonstratorTest extends BaseCardTest {

    @Test
    @DisplayName("Repartee gives +1/+0 and lifelink until end of turn")
    void reparteeBuffsAndGrantsLifelink() {
        harness.addToBattlefield(player1, new InkshapeDemonstrator());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent demonstrator = findPermanent(player1, "Inkshape Demonstrator");
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(4); // 3 base + 1
        assertThat(gqs.getEffectiveToughness(gd, demonstrator)).isEqualTo(4); // unchanged
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Repartee buff and lifelink wear off at cleanup")
    void reparteeWearsOff() {
        harness.addToBattlefield(player1, new InkshapeDemonstrator());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent demonstrator = findPermanent(player1, "Inkshape Demonstrator");
        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1); // exact cost, cannot pay Ward

        harness.castInstant(player2, 0, demonstrator.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(demonstrator.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature-targeting sorcery triggers Repartee")
    void sorceryTriggersRepartee() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, giant.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isTrue();
        assertThat(giant.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Repartee")
    void playerTargetDoesNotTriggerRepartee() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Targeting your own Demonstrator triggers Repartee without ward")
    void ownSpellTriggersReparteeWithoutWard() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, demonstrator.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isTrue();
        assertThat(demonstrator.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple creature-targeting casts accumulate the power bonus")
    void reparteeStacksForEachCast() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, giant.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, giant.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Paying ward permits the opponent's spell without triggering Repartee")
    void paidWardAllowsOpponentSpell() {
        Permanent demonstrator = harness.addToBattlefieldAndReturn(player1, new InkshapeDemonstrator());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, demonstrator.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(demonstrator.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, demonstrator)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demonstrator, Keyword.LIFELINK)).isFalse();
    }
}
