package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedemptorDreadnought.class, GrizzlyBears.class, SolRing.class})
class RedemptorDreadnoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card makes the attack trigger use its power")
    void boostsByPowerOfExiledCreature() {
        GrizzlyBears exiledCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledCreature));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();

        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(6);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack trigger does nothing when no card was exiled")
    void doesNothingWithoutExiledCard() {
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(4);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The additional cost accepts at most one creature card")
    void rejectsMoreThanOneExiledCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 1");
    }

    @Test
    @DisplayName("A creature in the graveyard need not be exiled")
    void canDeclineExileWithCreatureAvailable() {
        RedemptorDreadnought graveyardCard = new RedemptorDreadnought();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(dreadnought.getEffectivePower()).isEqualTo(4);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The additional cost cannot exile a noncreature card")
    void rejectsNoncreatureCard() {
        SolRing noncreature = new SolRing();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must match the additional cost");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    @DisplayName("Each Dreadnought uses only the card exiled with that spell")
    void keepsExileLinksSeparate() {
        harness.setGraveyard(player1, List.of(new RedemptorDreadnought()));
        harness.setHand(player1, List.of(new RedemptorDreadnought(), new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent withExile = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent withoutExile = gd.playerBattlefields.get(player1.getId()).get(1);
        withExile.setSummoningSick(false);
        withoutExile.setSummoningSick(false);
        assertThat(withExile.getEffectivePower()).isEqualTo(4);
        assertThat(withExile.getEffectiveToughness()).isEqualTo(4);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(withExile.getEffectivePower()).isEqualTo(8);
        assertThat(withExile.getEffectiveToughness()).isEqualTo(8);
        assertThat(withoutExile.getEffectivePower()).isEqualTo(4);
        assertThat(withoutExile.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack boost expires at cleanup without consuming the exiled card")
    void attackBoostExpiresAtCleanup() {
        RedemptorDreadnought exiledCreature = new RedemptorDreadnought();
        harness.setGraveyard(player1, List.of(exiledCreature));
        harness.setHand(player1, List.of(new RedemptorDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        Permanent dreadnought = gd.playerBattlefields.get(player1.getId()).getFirst();
        dreadnought.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(8);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(8);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dreadnought.getEffectivePower()).isEqualTo(4);
        assertThat(dreadnought.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCreature);
    }
}
