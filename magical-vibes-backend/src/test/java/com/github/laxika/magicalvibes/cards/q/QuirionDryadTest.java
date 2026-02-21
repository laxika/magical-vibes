package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuirionDryadTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Quirion Dryad has correct card properties")
    void hasCorrectProperties() {
        QuirionDryad card = new QuirionDryad();

        assertThat(card.getName()).isEqualTo("Quirion Dryad");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.DRYAD);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class);

        PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger =
                (PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect) card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).getFirst();
        assertThat(trigger.triggerColors()).isEqualTo(Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED));
        assertThat(trigger.amount()).isEqualTo(1);
        assertThat(trigger.onlyOwnSpells()).isTrue();
    }

    @Test
    @DisplayName("Casting a white spell makes Quirion Dryad get a +1/+1 counter")
    void ownWhiteSpellAddsCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent dryad = getDryad();
        assertThat(dryad.getPlusOnePlusOneCounters()).isZero();

        harness.castCreature(player1, 0);

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Quirion Dryad"))
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve dryad trigger

        assertThat(dryad.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, dryad)).isEqualTo(2);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, dryad)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a green spell does not trigger Quirion Dryad")
    void ownGreenSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent dryad = getDryad();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(dryad.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Opponent casting a white spell does not trigger Quirion Dryad")
    void opponentWhiteSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new QuirionDryad());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SuntailHawk()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        Permanent dryad = getDryad();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(dryad.getPlusOnePlusOneCounters()).isZero();
    }
    
    private Permanent getDryad() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Quirion Dryad"))
                .findFirst()
                .orElseThrow();
    }
}
