package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeylineOfPunishmentTest {

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

    // ===== Card structure =====

    @Test
    @DisplayName("Leyline of Punishment has PlayersCantGainLifeEffect and DamageCantBePreventedEffect as static effects")
    void hasStaticEffects() {
        LeylineOfPunishment card = new LeylineOfPunishment();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PlayersCantGainLifeEffect)
                .anyMatch(e -> e instanceof DamageCantBePreventedEffect);
    }

    @Test
    @DisplayName("Leyline of Punishment has ON_OPENING_HAND_REVEAL MayEffect wrapping LeylineStartOnBattlefieldEffect")
    void hasOpeningHandLeylineEffect() {
        LeylineOfPunishment card = new LeylineOfPunishment();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(LeylineStartOnBattlefieldEffect.class);
    }

    // ===== Leyline opening hand mechanic =====

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfPunishment()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfPunishment()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Punishment"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leyline of Punishment"));
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfPunishment()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of Punishment"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leyline of Punishment"));
    }

    // ===== Can be cast normally =====

    @Test
    @DisplayName("Leyline of Punishment can be cast normally for {2}{R}{R}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfPunishment()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Punishment"));
    }

    // ===== Players can't gain life =====

    @Test
    @DisplayName("Players can't gain life with Leyline of Punishment on the battlefield")
    void playersCantGainLife() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Give player2 the active turn to cast creatures
        harness.forceActivePlayer(player2);

        // Cast Angel of Mercy (ETB: gain 3 life) for player2
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        // Player2's life should remain at 20 — life gain was prevented
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Controller also can't gain life")
    void controllerCantGainLifeEither() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Cast Angel of Mercy (ETB: gain 3 life) for player1
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        // Player1's life should remain at 20 — life gain was prevented
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Life gain works again after Leyline of Punishment leaves the battlefield")
    void lifeGainWorksAfterLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        // Give player2 the active turn to cast creatures
        harness.forceActivePlayer(player2);

        // Cast Angel of Mercy (ETB: gain 3 life) for player2
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        // Player2's life should increase by 3
        harness.assertLife(player2, 23);
    }

    // ===== Damage can't be prevented =====

    @Test
    @DisplayName("Damage prevention shields are bypassed with Leyline of Punishment on the battlefield")
    void damagePreventionShieldsBypassed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Give player2 a damage prevention shield
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        // Shock player2 for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player2 should take full damage despite the shield
        harness.assertLife(player2, 18);
        // Shield should be unchanged (not consumed)
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Creature damage prevention shields are bypassed with Leyline of Punishment on the battlefield")
    void creatureDamagePreventionShieldsBypassed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Add a creature and give it a prevention shield
        harness.addToBattlefield(player2, new GrizzlyBears());
        var permanentId = harness.getPermanentId(player2, "Grizzly Bears");
        var permanent = gqs.findPermanentById(gd, permanentId);
        permanent.setDamagePreventionShield(5);

        // Shock the creature
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, permanentId);
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed by 2 damage despite the prevention shield
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage prevention works normally without Leyline of Punishment")
    void damagePreventionWorksWithoutLeyline() {
        harness.skipMulligan();

        // Give player2 a damage prevention shield (no leyline on battlefield)
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        // Shock player2 for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player2 should not take damage — shield prevented it
        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
    }

    // ===== Life loss is not prevented =====

    @Test
    @DisplayName("Players can still lose life (take damage) with Leyline of Punishment on the battlefield")
    void playersCanStillLoseLife() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfPunishment());

        // Shock player2 for 2 damage
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Damage goes through normally
        harness.assertLife(player2, 18);
    }
}
