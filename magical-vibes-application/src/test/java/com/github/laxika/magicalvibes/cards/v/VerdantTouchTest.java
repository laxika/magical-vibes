package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantTouch.class, Forest.class, TrainedArmodon.class})
class VerdantTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes a permanent 2/2 creature that is still a land")
    void animatesTargetLandPermanently() {
        Permanent land = addLand(player1);

        castVerdantTouch(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.isLand(gd, land)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Verdant Touch can target an opponent's land")
    void animatesOpponentsLand() {
        Permanent land = addLand(player2);

        castVerdantTouch(land);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("A land animated by Verdant Touch retains its mana ability")
    void animatedLandRetainsManaAbility() {
        Permanent land = addLand(player1);
        land.setSummoningSick(false);

        castVerdantTouch(land);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Without buyback Verdant Touch goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new VerdantTouch()));
        addManaForSpell(player1);

        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(playerHandNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Verdant Touch");
    }

    @Test
    @DisplayName("Paying buyback returns Verdant Touch to its owner's hand")
    void buybackReturnsToHand() {
        Permanent land = addLand(player1);
        harness.setHand(player1, List.of(new VerdantTouch()));
        addManaForSpell(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithBuyback(player1, 0, land.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Verdant Touch");
        assertThat(playerHandNames(player1)).containsExactly("Verdant Touch");
    }

    @Test
    @DisplayName("Verdant Touch cannot target a nonland permanent")
    void cannotTargetNonLand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        harness.setHand(player1, List.of(new VerdantTouch()));
        addManaForSpell(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void castVerdantTouch(Permanent target) {
        harness.setHand(player1, List.of(new VerdantTouch()));
        addManaForSpell(player1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addManaForSpell(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }

    private List<String> playerHandNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
