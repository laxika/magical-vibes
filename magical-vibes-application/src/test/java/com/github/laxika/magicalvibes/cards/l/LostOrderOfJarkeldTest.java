package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.b.BindingGrasp;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostOrderOfJarkeld.class, ArmorOfFaith.class, KjeldoranWarrior.class, ZuranOrb.class, BindingGrasp.class})
class LostOrderOfJarkeldTest extends BaseCardTest {

    @Test
    @DisplayName("Is 1/1 when the opponent controls no creatures")
    void isOneOneWithNoOpponentCreatures() {
        Permanent lostOrder = addLostOrder(player1);

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equal 1 plus opponent creature count")
    void ptEqualsOnePlusOpponentCreatures() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.addToBattlefield(player2, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count the controller's own creatures")
    void ignoresControllerCreatures() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player1, new KjeldoranWarrior());
        harness.addToBattlefield(player1, new KjeldoranWarrior());
        harness.addToBattlefield(player2, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only creatures controlled by the opponent")
    void ignoresOpponentNoncreatures() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new ZuranOrb());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates dynamically as opponent creatures enter and leave")
    void updatesDynamically() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(2);

        harness.addToBattlefield(player2, new KjeldoranWarrior());
        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Kjeldoran Warrior"));
        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(1);
    }

    @Test
    @DisplayName("CDA P/T stacks with static bonuses")
    void stacksWithStaticBonuses() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        Permanent armorOfFaith = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        armorOfFaith.setAttachedTo(lostOrder.getId());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Changing control retains the opponent chosen on entry")
    void retainsChosenOpponentAfterControlChange() {
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.castFromHand(player1, new LostOrderOfJarkeld(), "{2}{W}{W}");
        harness.passBothPriorities();
        Permanent lostOrder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof LostOrderOfJarkeld)
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new BindingGrasp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, lostOrder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(lostOrder);
        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(5);
    }

    @Test
    @DisplayName("Is 1/1 in hand, on the stack, and in the graveyard without a chosen opponent")
    void isOneOneOutsideBattlefield() {
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        LostOrderOfJarkeld card = new LostOrderOfJarkeld();
        harness.setHand(player1, List.of(card));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);

        harness.castFromHand(player1, card, "{2}{W}{W}");
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == card);
        harness.setGraveyard(player1, List.of(card));
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);
    }

    private Permanent addLostOrder(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LostOrderOfJarkeld());
    }
}
