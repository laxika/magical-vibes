package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificedPermanentSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainLanneryStormTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has CreateTokenEffect (Treasure artifact) on ON_ATTACK")
    void hasAttackTrigger() {
        CaptainLanneryStorm card = new CaptainLanneryStorm();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ATTACK).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Treasure");
        assertThat(effect.primaryType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Has SacrificedPermanentSubtypeConditionalEffect(TREASURE, BoostSelfEffect(1,0)) on ON_ALLY_PERMANENT_SACRIFICED")
    void hasSacrificeTrigger() {
        CaptainLanneryStorm card = new CaptainLanneryStorm();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED).getFirst())
                .isInstanceOf(SacrificedPermanentSubtypeConditionalEffect.class);
        SacrificedPermanentSubtypeConditionalEffect conditional =
                (SacrificedPermanentSubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED).getFirst();
        assertThat(conditional.requiredSubtype()).isEqualTo(CardSubtype.TREASURE);
        assertThat(conditional.wrapped()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect boost = (BoostSelfEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
    }

    // ===== ON_ATTACK — creates Treasure token =====

    @Test
    @DisplayName("Attacking creates a Treasure artifact token")
    void attackCreatesTreasureToken() {
        addCreatureReady(player1, new CaptainLanneryStorm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Should have a Treasure token on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Treasure")
                        && p.getCard().getType() == CardType.ARTIFACT
                        && p.getCard().getSubtypes().contains(CardSubtype.TREASURE)
                        && p.getCard().isToken());
    }

    @Test
    @DisplayName("Treasure token has activated ability for mana")
    void treasureTokenHasActivatedAbility() {
        addCreatureReady(player1, new CaptainLanneryStorm());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent treasure = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .findFirst().orElseThrow();

        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(treasure.getCard().getActivatedAbilities().getFirst().getDescription())
                .contains("Add one mana of any color");
    }

    // ===== ON_ALLY_PERMANENT_SACRIFICED — +1/+0 when Treasure sacrificed =====

    @Test
    @DisplayName("Gets +1/+0 when a Treasure is sacrificed")
    void boostsWhenTreasureSacrificed() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        // Create a Treasure token by attacking
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Find the Treasure token index on the battlefield
        int treasureIndex = -1;
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Treasure")) {
                treasureIndex = i;
                break;
            }
        }
        assertThat(treasureIndex).isGreaterThanOrEqualTo(0);

        // Move to main phase so we can activate abilities
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Activate the Treasure token's ability (tap + sacrifice → add mana)
        harness.activateAbility(player1, treasureIndex, null, null);

        // The Treasure sacrifice trigger should have fired, putting +1/+0 on stack
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing multiple Treasures gives cumulative +1/+0 boosts")
    void multiTreasureSacrificeGivesCumulativeBoost() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        // Manually create two Treasure tokens
        addTreasureToken(player1);
        addTreasureToken(player1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        int firstTreasure = -1;
        int secondTreasure = -1;
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Treasure")) {
                if (firstTreasure == -1) firstTreasure = i;
                else secondTreasure = i;
            }
        }

        // Sacrifice first treasure
        harness.activateAbility(player1, firstTreasure, null, null);
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        // Sacrifice second treasure (index shifted after first was removed)
        // Re-find the remaining treasure
        int remainingTreasure = -1;
        bf = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Treasure")) {
                remainingTreasure = i;
                break;
            }
        }
        harness.activateAbility(player1, remainingTreasure, null, null);
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a non-Treasure does not trigger +1/+0")
    void nonTreasureSacrificeDoesNotTrigger() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        // Add a creature that can sacrifice itself (like a generic creature)
        Card creature = new Card();
        creature.setName("Goblin Token");
        creature.setType(CardType.CREATURE);
        creature.setSubtypes(List.of(CardSubtype.GOBLIN));
        creature.setPower(1);
        creature.setToughness(1);
        creature.setToken(true);
        Permanent goblin = new Permanent(creature);
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        // Simulate sacrificing the goblin manually (remove from battlefield, fire trigger)
        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        gd.playerGraveyards.get(player1.getId()).add(goblin.getCard());
        harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), goblin.getCard());
        resolveAllTriggers();

        // Captain should NOT get +1/+0 because a Goblin was sacrificed, not a Treasure
        assertThat(captain.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+1/+0 modifier resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new CaptainLanneryStorm());

        addTreasureToken(player1);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        int treasureIndex = -1;
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i).getCard().getName().equals("Treasure")) {
                treasureIndex = i;
                break;
            }
        }

        harness.activateAbility(player1, treasureIndex, null, null);
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captain.getPowerModifier()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setManaCost("");
        treasureCard.setToken(true);
        treasureCard.setColor(null);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new com.github.laxika.magicalvibes.model.ActivatedAbility(
                true,
                null,
                List.of(new com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost(),
                        new com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));
        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
