package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MortivoreTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Mortivore has correct card properties")
    void hasCorrectProperties() {
        Mortivore card = new Mortivore();

        assertThat(card.getName()).isEqualTo("Mortivore");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.LHURGOYF);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCreatureCardsInAllGraveyardsEffect.class);
    }

    @Test
    @DisplayName("Mortivore has regeneration activated ability costing {B}")
    void hasRegenerationAbility() {
        Mortivore card = new Mortivore();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Mortivore puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Mortivore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mortivore");
    }

    @Test
    @DisplayName("Resolving Mortivore puts it on the battlefield when graveyard has creatures")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setHand(player1, List.of(new Mortivore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mortivore"));
    }

    @Test
    @DisplayName("Mortivore dies to state-based actions when no creatures in any graveyard")
    void diesWhenNoCreaturesInGraveyards() {
        harness.setHand(player1, List.of(new Mortivore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mortivore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mortivore"));
    }

    // ===== Dynamic power/toughness =====

    @Test
    @DisplayName("Mortivore is 0/0 with no creature cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addMortivoreReady(player1);

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Mortivore P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mortivore P/T counts creature cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Mortivore only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addMortivoreReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mortivore P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mortivore P/T decreases when creatures are removed from graveyard")
    void ptDecreasesWhenCreaturesRemovedFromGraveyard() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(5));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mortivore P/T counts opponent's graveyard creatures too")
    void ptCountsOpponentsGraveyard() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mortivore P/T works with large graveyard counts")
    void ptWorksWithLargeGraveyardCounts() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(15));
        harness.setGraveyard(player2, createCreatureCards(10));

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(25);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(25);
    }

    // ===== P/T interacts with other static effects =====

    @Test
    @DisplayName("Mortivore P/T stacks with other static bonuses")
    void ptStacksWithOtherStaticBonuses() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        // Add a Glorious Anthem for +1/+1 to own creatures
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GloriousAnthem());

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mortivore P/T stacks with temporary power modifiers")
    void ptStacksWithTemporaryModifiers() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        perm.setPowerModifier(2);
        perm.setToughnessModifier(2);

        assertThat(gs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    // ===== Regeneration ability =====

    @Test
    @DisplayName("Activating regeneration puts ability on stack with self as target")
    void activatingRegenPutsOnStack() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mortivore");
        assertThat(entry.getTargetPermanentId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenShields() {
        Permanent perm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getRegenerationShield()).isEqualTo(2);
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Mortivore from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent mortPerm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2)); // Mortivore is 2/2
        mortPerm.setRegenerationShield(1);
        mortPerm.setBlocking(true);
        mortPerm.addBlockingTarget(0);

        // Grizzly Bears (2/2) deals 2 damage — lethal to 2-toughness Mortivore
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Mortivore should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mortivore"));
        Permanent mort = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mortivore"))
                .findFirst().orElseThrow();
        assertThat(mort.isTapped()).isTrue();
        assertThat(mort.getRegenerationShield()).isEqualTo(0);
        // Grizzly Bears also takes 2 lethal damage from Mortivore and dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mortivore dies without regeneration shield when taking lethal combat damage")
    void diesWithoutRegenShield() {
        Permanent mortPerm = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2)); // Mortivore is 2/2
        mortPerm.setBlocking(true);
        mortPerm.addBlockingTarget(0);

        // Grizzly Bears (2/2) deals 2 damage — lethal to 2-toughness Mortivore
        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both creatures trade — Mortivore has no regeneration shield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mortivore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mortivore"));
    }

    @Test
    @DisplayName("Mortivore going to graveyard increases other Mortivore's P/T")
    void dyingMortivoreIncreasesOtherMortivorePT() {
        Permanent mort1 = addMortivoreReady(player1);
        Permanent mort2 = addMortivoreReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3)); // Both are 3/3

        assertThat(gs.getEffectivePower(gd, mort1)).isEqualTo(3);

        // Simulate mort2 dying — move its card to graveyard
        gd.playerBattlefields.get(player1.getId()).remove(mort2);
        gd.playerGraveyards.get(player1.getId()).add(mort2.getCard());

        // mort1 should now be 4/4 (3 original creatures + Mortivore in graveyard)
        assertThat(gs.getEffectivePower(gd, mort1)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, mort1)).isEqualTo(4);
    }

    // ===== Helper methods =====

    private Permanent addMortivoreReady(Player player) {
        Mortivore card = new Mortivore();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
