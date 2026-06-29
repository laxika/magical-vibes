package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreechingSilcawTest extends BaseCardTest {

    private Permanent addReadyCreature(Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.put(player2.getId(), new ArrayList<>(cards));
    }

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has metalcraft-conditional combat damage mill effect")
    void hasCorrectEffect() {
        ScreechingSilcaw card = new ScreechingSilcaw();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft =
                (MetalcraftConditionalEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(MillTargetPlayerEffect.class);

        MillTargetPlayerEffect mill = (MillTargetPlayerEffect) metalcraft.wrapped();
        assertThat(mill.count()).isEqualTo(4);
    }

    // ===== Combat damage with metalcraft met =====

    @Test
    @DisplayName("Dealing combat damage mills 4 cards when metalcraft is met")
    void millsFourCardsWithMetalcraft() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Samite Healer");
    }

    @Test
    @DisplayName("Milled cards go to graveyard in order from top of library")
    void milledCardsGoToGraveyard() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer(),
                new Scalpelexis()
        ));

        resolveCombat();

        List<Card> graveyard = gd.playerGraveyards.get(player2.getId());
        assertThat(graveyard).extracting(Card::getName)
                .contains("Grizzly Bears", "Serra Angel", "Suntail Hawk", "Samite Healer");
    }

    @Test
    @DisplayName("Game log records metalcraft mill trigger")
    void gameLogRecordsMillTrigger() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("metalcraft ability triggers"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("mills 4 card"));
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Silcaw")
    void defenderTakesCombatDamage() {
        setupMetalcraft();
        harness.setLife(player2, 20);
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Combat damage without metalcraft =====

    @Test
    @DisplayName("No mill when metalcraft is not met (0 artifacts)")
    void noMillWithoutMetalcraft() {
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("No mill with only 2 artifacts")
    void noMillWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Defender still takes combat damage even without metalcraft")
    void defenderTakesDamageWithoutMetalcraft() {
        harness.setLife(player2, 20);
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("No trigger when Silcaw is blocked")
    void noTriggerWhenBlocked() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(3); // Silcaw is at index 3 (after 3 Spellbooks)

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Handles library with fewer than 4 cards")
    void partialLibraryMill() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Handles empty library gracefully")
    void emptyLibrary() {
        setupMetalcraft();
        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of());

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mills with exactly 3 artifacts (metalcraft threshold)")
    void millsWithExactlyThreeArtifacts() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent silcaw = addReadyCreature(new ScreechingSilcaw());
        silcaw.setAttacking(true);

        setDeck(List.of(
                new GrizzlyBears(),
                new SerraAngel(),
                new SuntailHawk(),
                new SamiteHealer()
        ));

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
