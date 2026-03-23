package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlimefootTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two ON_ALLY_CREATURE_DIES effects with SubtypeConditionalEffect wrapping damage and life gain")
    void hasCorrectDeathTriggerStructure() {
        Slimefoot card = new Slimefoot();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(2);

        // First effect: deal 1 damage to each opponent
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(0))
                .isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect damageFilter =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(0);
        assertThat(damageFilter.subtype()).isEqualTo(CardSubtype.SAPROLING);
        assertThat(damageFilter.wrapped()).isInstanceOf(DealDamageToEachOpponentEffect.class);
        assertThat(((DealDamageToEachOpponentEffect) damageFilter.wrapped()).damage()).isEqualTo(1);

        // Second effect: gain 1 life
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(1))
                .isInstanceOf(SubtypeConditionalEffect.class);
        SubtypeConditionalEffect lifeFilter =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).get(1);
        assertThat(lifeFilter.subtype()).isEqualTo(CardSubtype.SAPROLING);
        assertThat(lifeFilter.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) lifeFilter.wrapped()).amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has activated ability to create 1/1 green Saproling token for {4}")
    void hasCorrectActivatedAbility() {
        Slimefoot card = new Slimefoot();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{4}");
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) card.getActivatedAbilities().get(0).getEffects().get(0);
        assertThat(tokenEffect.tokenName()).isEqualTo("Saproling");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.GREEN);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    // ===== Token creation via activated ability =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Slimefoot, the Stowaway");
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 green Saproling token")
    void resolvingAbilityCreatesToken() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    // ===== Death trigger: Saproling dies =====

    @Test
    @DisplayName("Deals 1 damage to each opponent and gains 1 life when a Saproling dies")
    void triggersWhenSaprolingDies() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Create a Saproling token
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill the Saproling with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saprolingId = harness.getPermanentId(player1, "Saproling");
        harness.castInstant(player2, 0, saprolingId);
        harness.passBothPriorities(); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);
    }

    @Test
    @DisplayName("Does NOT trigger when a non-Saproling creature dies")
    void doesNotTriggerForNonSaproling() {
        addSlimefootReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill the Bears with Shock
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → Bears die

        // No trigger should have fired — life totals unchanged
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore);
    }

    @Test
    @DisplayName("Triggers multiple times when multiple Saprolings die")
    void triggersForEachSaprolingDeath() {
        addSlimefootReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        // Create two Saproling tokens
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long saprolingCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .count();
        assertThat(saprolingCount).isEqualTo(2);

        int p1LifeBefore = gd.getLife(player1.getId());
        int p2LifeBefore = gd.getLife(player2.getId());

        // Kill first Saproling
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saproling1Id = harness.getPermanentId(player1, "Saproling");
        harness.castInstant(player2, 0, saproling1Id);
        harness.passBothPriorities(); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 1);

        // Kill second Saproling
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        java.util.UUID saproling2Id = harness.getPermanentId(player1, "Saproling");
        harness.castInstant(player2, 0, saproling2Id);
        harness.passBothPriorities(); // Resolve Shock → Saproling dies → death trigger
        harness.passBothPriorities(); // Resolve Slimefoot's trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 2);
    }

    // ===== Helper methods =====

    private Permanent addSlimefootReady(Player player) {
        Slimefoot card = new Slimefoot();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
