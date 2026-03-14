package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeylineOfVitalityTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — leyline tests need to set hand first
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Leyline of Vitality has leyline opening hand effect")
    void hasOpeningHandLeylineEffect() {
        LeylineOfVitality card = new LeylineOfVitality();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(LeylineStartOnBattlefieldEffect.class);
    }

    @Test
    @DisplayName("Leyline of Vitality has StaticBoostEffect +0/+1 for own creatures")
    void hasStaticBoostEffect() {
        LeylineOfVitality card = new LeylineOfVitality();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(0);
        assertThat(boost.toughnessBoost()).isEqualTo(1);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("Leyline of Vitality has MayEffect wrapping GainLifeEffect(1) on ally creature enters")
    void hasCreatureEntersLifeGainEffect() {
        LeylineOfVitality card = new LeylineOfVitality();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) may.wrapped()).amount()).isEqualTo(1);
    }

    // ===== Leyline opening hand mechanic (CR 103.6) =====

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Vitality"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leyline of Vitality"));
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of Vitality"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leyline of Vitality"));
    }

    // ===== Static +0/+1 to own creatures =====

    @Test
    @DisplayName("Own creatures get +0/+1")
    void buffsOwnCreaturesToughness() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs all own creatures")
    void buffsAllOwnCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(gqs.getEffectivePower(gd, p))
                        .isEqualTo(p.getCard().getPower());
                assertThat(gqs.getEffectiveToughness(gd, p))
                        .isEqualTo(p.getCard().getToughness() + 1);
            }
        }
    }

    @Test
    @DisplayName("Two Leylines give +0/+2")
    void twoLeylinesStack() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Leyline of Vitality leaves the battlefield")
    void bonusRemovedWhenLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Leyline of Vitality"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Triggered ability: creature enters, may gain 1 life =====

    @Test
    @DisplayName("Creature entering triggers may prompt, accepting gains 1 life")
    void creatureEnteringTriggersLifeGainAccepted() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        // Resolve creature spell — creature enters, MayEffect trigger goes on stack
        harness.passBothPriorities();
        // Resolve MayEffect from stack -> may prompt
        harness.passBothPriorities();

        // May prompt should be awaiting input
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Creature entering triggers may prompt, declining gains no life")
    void creatureEnteringTriggersLifeGainDeclined() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfVitality());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        // Resolve creature spell — creature enters, MayEffect trigger goes on stack
        harness.passBothPriorities();
        // Resolve MayEffect from stack -> may prompt
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for opponent's creatures entering")
    void doesNotTriggerForOpponentCreatures() {
        harness.skipMulligan();
        // Player1's Leyline on battlefield
        harness.addToBattlefield(player1, new LeylineOfVitality());

        // Player2 casts a creature on their turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);
        // Resolve creature spell
        harness.passBothPriorities();

        // No may prompt for player1 — Leyline only triggers for its controller's creatures
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leyline of Vitality"));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Leyline can be cast normally from hand =====

    @Test
    @DisplayName("Leyline of Vitality can be cast normally for {2}{G}{G}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Vitality"));
    }

    // ===== Leyline pregame + static boost interaction =====

    @Test
    @DisplayName("Leyline placed from opening hand immediately buffs creatures that enter afterward")
    void leylineFromOpeningHandBuffsCreatures() {
        harness.setHand(player1, List.of(new LeylineOfVitality()));
        harness.skipMulligan();
        harness.handleMayAbilityChosen(player1, true);

        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
