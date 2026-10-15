package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkFavor.class, RuneclawBear.class, Unsummon.class})
class DarkFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Favor attaches it, boosts the creature by +3/+1 and costs 1 life")
    void castAttachesBoostsAndCostsLife() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkFavor()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dark Favor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost goes away when Dark Favor leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DarkFavor());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature boosts it immediately but only the Aura controller loses life when the trigger resolves")
    void opponentCreatureAndSeparateLifeLossTrigger() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new DarkFavor()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bear.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dark Favor").getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Dark Favor does not enter or cause life loss when its target leaves before resolution")
    void missingTargetDoesNotCauseLifeLoss() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new DarkFavor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.castInstant(player2, 0, bear.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dark Favor")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof DarkFavor);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The life loss trigger resolves even if the enchanted creature and Aura leave first")
    void lifeLossTriggerSurvivesAuraLeaving() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new DarkFavor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dark Favor")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof DarkFavor);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
