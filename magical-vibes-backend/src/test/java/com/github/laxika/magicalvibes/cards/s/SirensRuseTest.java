package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnImmediatelyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SirensRuseTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Card has ExileTargetPermanentAndReturnImmediatelyEffect with Pirate bonus on SPELL slot")
    void hasCorrectEffect() {
        SirensRuse card = new SirensRuse();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ExileTargetPermanentAndReturnImmediatelyEffect.class);
        ExileTargetPermanentAndReturnImmediatelyEffect effect =
                (ExileTargetPermanentAndReturnImmediatelyEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.bonusSubtype()).isEqualTo(CardSubtype.PIRATE);
        assertThat(effect.bonusEffect()).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) effect.bonusEffect()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Target filter requires creature controlled by source controller")
    void hasCorrectTargetFilter() {
        SirensRuse card = new SirensRuse();

        assertThat(card.getSpellTargets()).hasSize(1);
        assertThat(card.getSpellTargets().getFirst().getFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter =
                (PermanentPredicateTargetFilter) card.getSpellTargets().getFirst().getFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentAllOfPredicate.class);
        PermanentAllOfPredicate allOf = (PermanentAllOfPredicate) filter.predicate();
        assertThat(allOf.predicates()).hasSize(2);
        assertThat(allOf.predicates()).anySatisfy(p ->
                assertThat(p).isInstanceOf(PermanentControlledBySourceControllerPredicate.class));
        assertThat(allOf.predicates()).anySatisfy(p ->
                assertThat(p).isInstanceOf(PermanentIsCreaturePredicate.class));
    }

    // ===== Flicker non-Pirate creature =====

    @Test
    @DisplayName("Flickers a non-Pirate creature without drawing a card")
    void flickerNonPirateNoDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve

        // Creature should be back on battlefield (new permanent)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Should not be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // No card drawn (hand size should not increase — the spell was cast from hand so -1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    // ===== Flicker Pirate creature =====

    @Test
    @DisplayName("Flickers a Pirate creature and draws a card")
    void flickerPirateDrawsCard() {
        harness.addToBattlefield(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID pirateId = harness.getPermanentId(player1, "Dire Fleet Captain");
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0, pirateId);
        harness.passBothPriorities(); // resolve

        // Pirate should be back on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dire Fleet Captain"));
        // Should not be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Dire Fleet Captain"));
        // Drew a card (hand = before - 1 spell + 1 draw = same)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    // ===== Returned creature has summoning sickness =====

    @Test
    @DisplayName("Returned creature has summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    // ===== Cannot target opponent's creature =====

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Returns under owner's control (not controller's) =====

    @Test
    @DisplayName("Returned creature goes to its owner (not necessarily the controller)")
    void returnsUnderOwnersControl() {
        // Player1 steals player2's creature, then flickers it — should return to player2 (owner)
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        // Simulate stolen creature: register in stolenCreatures map
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        gd.stolenCreatures.put(bearsPermId, player2.getId());

        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passBothPriorities();

        // Should return under player2's control (the owner)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Fizzles if target is removed =====

    @Test
    @DisplayName("Spell fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SirensRuse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        // Remove the creature before the spell resolves
        Permanent bearsPerm = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        // Stack should be empty, no crash
        assertThat(gd.stack).isEmpty();
    }
}
