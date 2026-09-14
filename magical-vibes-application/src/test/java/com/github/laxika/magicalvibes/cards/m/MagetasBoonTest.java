package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RootCage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagetasBoon.class, PygmyRazorback.class, RootCage.class})
class MagetasBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Mageta's Boon during an opponent's turn thanks to flash")
    void canCastDuringOpponentsTurn() {
        Permanent boar = addCreatureReady(player2, new PygmyRazorback());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new MagetasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castEnchantment(player1, 0, boar.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Mageta's Boon attaches it and boosts the enchanted creature")
    void resolvesAndBoostsEnchantedCreature() {
        Permanent boar = addCreatureReady(player1, new PygmyRazorback());
        harness.setHand(player1, List.of(new MagetasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, boar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MagetasBoon
                        && boar.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mageta's Boon boosts a creature controlled by an opponent")
    void boostsOpponentCreature() {
        Permanent boar = addCreatureReady(player2, new PygmyRazorback());
        harness.setHand(player1, List.of(new MagetasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, boar.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing Mageta's Boon removes its static boost")
    void boostStopsWhenRemoved() {
        Permanent boar = addCreatureReady(player1, new PygmyRazorback());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MagetasBoon());
        aura.setAttachedTo(boar.getId());

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mageta's Boon fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent boar = addCreatureReady(player1, new PygmyRazorback());
        harness.setHand(player1, List.of(new MagetasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, boar.getId());
        gd.playerBattlefields.get(player1.getId()).remove(boar);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mageta's Boon");
        harness.assertNotOnBattlefield(player1, "Mageta's Boon");
    }

    @Test
    @DisplayName("Mageta's Boon cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new RootCage());
        harness.setHand(player1, List.of(new MagetasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
