package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VedalkenAnatomistTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with tap, {2}{U} cost, -1/-1 counter and may tap/untap effects")
    void hasCorrectStructure() {
        VedalkenAnatomist card = new VedalkenAnatomist();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}{U}");
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(2);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(1))
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getActivatedAbilities().get(0).getEffects().get(1);
        assertThat(mayEffect.wrapped()).isInstanceOf(TapOrUntapTargetPermanentEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Activation puts ability on stack =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a creature")
    void activatingPutsOnStack() {
        Permanent anatomist = addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vedalken Anatomist");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Vedalken Anatomist")
    void activatingTapsAnatomist() {
        Permanent anatomist = addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(anatomist.isTapped()).isTrue();
    }

    // ===== Puts -1/-1 counter on target =====

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature and prompts may choice")
    void putsCounterAndPromptsMayChoice() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // -1/-1 counter should be placed
        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        // May ability should be pending
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== May tap/untap — accept taps untapped creature =====

    @Test
    @DisplayName("Accepting may choice taps an untapped target creature")
    void acceptingMayTapsUntappedCreature() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    // ===== May tap/untap — accept untaps tapped creature =====

    @Test
    @DisplayName("Accepting may choice untaps a tapped target creature")
    void acceptingMayUntapsTappedCreature() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    // ===== May tap/untap — decline leaves creature unchanged =====

    @Test
    @DisplayName("Declining may choice leaves untapped creature untapped")
    void decliningMayLeavesCreatureUntapped() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        assertThat(target.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Counter should still be placed
        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);
        // Creature should remain untapped
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining may choice leaves tapped creature tapped")
    void decliningMayLeavesCreatureTapped() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
    }

    // ===== Kills creature with -1/-1 counter =====

    @Test
    @DisplayName("Kills a 1/1 creature with -1/-1 counter (SBA before may choice)")
    void killsOneOneCreature() {
        addReadyAnatomist(player1);
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Llanowar Elves (1/1) should die from 0 toughness
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Can target own creatures =====

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        addReadyAnatomist(player1);
        Permanent ownCreature = addReadyCreature(player1);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyAnatomist(player1);
        AngelsFeather artifact = new AngelsFeather();
        Permanent artifactPerm = new Permanent(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifactPerm);
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Costs =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent anatomist = addReadyAnatomist(player1);
        anatomist.tap();
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        VedalkenAnatomist card = new VedalkenAnatomist();
        Permanent anatomist = new Permanent(card);
        // summoningSick is true by default
        gd.playerBattlefields.get(player1.getId()).add(anatomist);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyAnatomist(player1);
        Permanent target = addReadyCreature(player2);
        addAnatomistMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyAnatomist(Player player) {
        VedalkenAnatomist card = new VedalkenAnatomist();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAnatomistMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
