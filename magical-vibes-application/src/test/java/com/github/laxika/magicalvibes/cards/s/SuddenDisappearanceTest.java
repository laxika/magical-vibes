package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SuddenDisappearanceTest extends BaseCardTest {

    @Test
    @DisplayName("Sudden Disappearance has correct effect configuration")
    void hasCorrectEffectConfiguration() {
        SuddenDisappearance card = new SuddenDisappearance();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect.class);
    }

    @Test
    @DisplayName("Casting puts it on the stack as SORCERY_SPELL targeting a player")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sudden Disappearance");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Exiles all nonland permanents target player controls")
    void exilesAllNonlandPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"))
                .noneMatch(p -> p.getCard().getName().equals("Glorious Anthem"));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Gold Myr", "Glorious Anthem");
        assertThat(gd.pendingExileReturns).hasSize(3);
    }

    @Test
    @DisplayName("Does not exile lands the target player controls")
    void doesNotExileLands() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears")
                .doesNotContain("Forest");
        assertThat(gd.pendingExileReturns).hasSize(1);
    }

    @Test
    @DisplayName("Does not affect the other player's permanents")
    void doesNotAffectOtherPlayersNonlandPermanents() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        assertThat(gd.pendingExileReturns).hasSize(1);
    }

    @Test
    @DisplayName("Returns exiled permanents at beginning of next end step under owner's control")
    void returnsExiledPermanentsAtNextEndStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingExileReturns).hasSize(2);

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .anyMatch(p -> p.getCard().getName().equals("Gold Myr"));
        assertThat(gd.pendingExileReturns).isEmpty();
    }

    @Test
    @DisplayName("Can target self to exile own nonland permanents")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"))
                .noneMatch(p -> p.getCard().getName().equals("Gold Myr"));
        assertThat(gd.pendingExileReturns).hasSize(2);
    }

    @Test
    @DisplayName("Works when target player has no nonland permanents")
    void worksWithNoNonlandPermanents() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.pendingExileReturns).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sudden Disappearance goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sudden Disappearance"));
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
