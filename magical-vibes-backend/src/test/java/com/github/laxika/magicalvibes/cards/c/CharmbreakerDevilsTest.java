package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CharmbreakerDevilsTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has upkeep-triggered random graveyard return and spell cast trigger boost")
    void hasCorrectEffects() {
        CharmbreakerDevils card = new CharmbreakerDevils();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
        ReturnCardFromGraveyardEffect returnEffect =
                (ReturnCardFromGraveyardEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(returnEffect.returnAtRandom()).isTrue();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect castTrigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(castTrigger.resolvedEffects()).hasSize(1);
        assertThat(castTrigger.resolvedEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boost = (BoostSelfEffect) castTrigger.resolvedEffects().getFirst();
        assertThat(boost.powerBoost()).isEqualTo(4);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    // ===== Upkeep trigger: return random instant or sorcery =====

    @Test
    @DisplayName("Upkeep returns an instant from graveyard to hand at random")
    void upkeepReturnsInstantFromGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve trigger — random return (no graveyard choice prompt since it's random)
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Upkeep returns a random card when multiple instants/sorceries in graveyard")
    void upkeepReturnsRandomFromMultipleInstantsSorceries() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new LightningBolt(), new Shock()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // One of the two should be returned to hand
        long handInstants = gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Lightning Bolt") || c.getName().equals("Shock"))
                .count();
        assertThat(handInstants).isEqualTo(1);

        // One should remain in graveyard
        long graveyardInstants = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Lightning Bolt") || c.getName().equals("Shock"))
                .count();
        assertThat(graveyardInstants).isEqualTo(1);
    }

    @Test
    @DisplayName("No effect when graveyard has no instants or sorceries")
    void noEffectWithNoInstantsOrSorceries() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        // Trigger fires but should resolve without returning anything
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Should resolve without error
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Upkeep trigger ignores creature cards and only returns instant/sorcery")
    void upkeepIgnoresCreaturesInGraveyard() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Lightning Bolt should be returned (it's the only instant/sorcery)
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        // Grizzly Bears should stay in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CharmbreakerDevils());
        harness.setGraveyard(player1, List.of(new LightningBolt()));

        advanceToUpkeep(player2);

        // No trigger should fire
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    // ===== Spell cast trigger: +4/+0 =====

    @Test
    @DisplayName("Casting an instant spell gives +4/+0 until end of turn")
    void castingInstantGivesBoost() {
        Permanent devils = addReadyDevils(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        // Shock targets any target — target opponent
        harness.castInstant(player1, 0, player2.getId());

        // Spell cast trigger fires — resolve the +4/+0 boost
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isEqualTo(4);
        assertThat(devils.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-instant/sorcery spell does not give +4/+0")
    void castingCreatureDoesNotGiveBoost() {
        Permanent devils = addReadyDevils(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        // No spell cast trigger — resolve Grizzly Bears
        harness.passBothPriorities();

        assertThat(devils.getPowerModifier()).isEqualTo(0);
        assertThat(devils.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple instant/sorcery casts stack the boost")
    void multipleInstantCastsStackBoost() {
        Permanent devils = addReadyDevils(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast first instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());

        // Resolve spell cast trigger (+4/+0)
        harness.passBothPriorities();
        assertThat(devils.getPowerModifier()).isEqualTo(4);

        // Resolve Shock
        harness.passBothPriorities();

        // Cast second instant
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());

        // Resolve second spell cast trigger (+4/+0 again)
        harness.passBothPriorities();
        assertThat(devils.getPowerModifier()).isEqualTo(8);
    }

    // ===== Helper methods =====

    private Permanent addReadyDevils(Player player) {
        CharmbreakerDevils card = new CharmbreakerDevils();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
