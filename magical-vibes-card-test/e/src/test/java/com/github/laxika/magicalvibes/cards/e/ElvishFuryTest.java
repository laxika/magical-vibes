package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishFury.class, WindDrake.class, Counterspell.class, DarkBanishing.class})
class ElvishFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Elvish Fury gives the target creature +2/+2")
    void boostsTarget() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent drake = windDrake(player1);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Elvish Fury can target an opponent's creature")
    void boostsOpponentCreature() {
        harness.addToBattlefield(player2, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Wind Drake");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent drake = windDrake(player2);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent drake = windDrake(player1);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Elvish Fury");
    }

    @Test
    @DisplayName("Paying buyback {4} returns the spell to hand and still boosts")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castInstantWithBuyback(player1, 0, targetId);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Elvish Fury");
        assertThat(handNames(player1)).containsExactly("Elvish Fury");
        assertThat(gqs.getEffectivePower(gd, windDrake(player1))).isEqualTo(4);
    }

    @Test
    @DisplayName("Paying buyback without enough mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Elvish Fury");
    }

    @Test
    @DisplayName("Elvish Fury cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback does not return Elvish Fury when its target is destroyed in response")
    void buybackDoesNotReturnWithIllegalTarget() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wind Drake");
        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).contains("Elvish Fury", "Wind Drake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering Elvish Fury prevents both the boost and the buyback return")
    void counteredBuybackSpellGoesToGraveyard() {
        harness.addToBattlefield(player1, new WindDrake());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Wind Drake");
        harness.castInstantWithBuyback(player1, 0, targetId);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castAndResolveInstant(player2, 0, spellId);

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Elvish Fury");
        assertThat(gqs.getEffectivePower(gd, windDrake(player1))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, windDrake(player1))).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent windDrake(Player player) {
        return findPermanent(player, "Wind Drake");
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
