package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FearTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Fear has correct card properties")
    void hasCorrectProperties() {
        Fear card = new Fear();

        assertThat(card.getName()).isEqualTo("Fear");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordToEnchantedCreatureEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Fear puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fear");
    }

    @Test
    @DisplayName("Resolving Fear attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fear")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Grants fear keyword =====

    @Test
    @DisplayName("Enchanted creature has fear")
    void enchantedCreatureHasFear() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isTrue();
    }

    // ===== Blocking restrictions =====

    @Test
    @DisplayName("Creature with fear cannot be blocked by non-black non-artifact creature")
    void cannotBeBlockedByNonBlackNonArtifactCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        // Blocker: GrizzlyBears (green, non-artifact) on player2
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Creature with fear can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        // Blocker: DrossCrocodile (black creature) on player2
        Permanent blockerPerm = new Permanent(new DrossCrocodile());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // declareBlockers succeeds without throwing — black creature can block fear
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Creature with fear can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        // Attacker: GrizzlyBears enchanted with Fear (player1)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(attackerPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        // Blocker: Chimeric Staff animated as artifact creature on player2
        Permanent staffPerm = new Permanent(new com.github.laxika.magicalvibes.cards.c.ChimericStaff());
        staffPerm.setSummoningSick(false);
        staffPerm.setAnimatedUntilEndOfTurn(true);
        staffPerm.setAnimatedPower(3);
        staffPerm.setAnimatedToughness(3);
        gd.playerBattlefields.get(player2.getId()).add(staffPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // declareBlockers succeeds without throwing — artifact creature can block fear
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    // ===== Effects stop when removed =====

    @Test
    @DisplayName("Creature loses fear when Fear aura is removed")
    void effectsStopWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        // Verify fear is active
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isTrue();

        // Remove Fear aura
        gd.playerBattlefields.get(player1.getId()).remove(fearPerm);

        // Verify fear is gone
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FEAR)).isFalse();
    }

    // ===== Does not affect other creatures =====

    @Test
    @DisplayName("Fear does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent fearPerm = new Permanent(new Fear());
        fearPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(fearPerm);

        // Other creature should not have fear
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FEAR)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fear fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Fear()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        // Fear should go to graveyard, not battlefield
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fear"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fear"));
    }

    // ===== Creature with innate fear keyword =====

    @Test
    @DisplayName("Creature with fear keyword granted directly cannot be blocked by non-black non-artifact creature")
    void innateKeywordBlockingRestriction() {
        // Simulate a creature that has fear as an innate keyword
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);
        attackerPerm.getGrantedKeywords().add(Keyword.FEAR);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        // Blocker: GrizzlyBears (green) on player2
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }
}
