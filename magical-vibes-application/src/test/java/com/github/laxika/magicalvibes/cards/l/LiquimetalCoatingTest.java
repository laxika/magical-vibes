package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiquimetalCoating.class, AngelsFeather.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, SongOfTheDryads.class})
class LiquimetalCoatingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LiquimetalCoating()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(LiquimetalCoating.class);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LiquimetalCoating()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Liquimetal Coating");
    }

    @Test
    @DisplayName("Activating ability targets a creature and puts ability on the stack")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability makes target creature an artifact")
    void resolvingMakesCreatureAnArtifact() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Target creature retains its creature type")
    void targetRetainsCreatureType() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Can target a land and make it an artifact")
    void canTargetLand() {
        addReadyCoating(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(targetLand)).isTrue();
    }

    @Test
    @DisplayName("Can target an enchantment and make it an artifact")
    void canTargetEnchantment() {
        addReadyCoating(player1);
        Permanent enchantment = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(enchantment)).isTrue();
    }

    @Test
    @DisplayName("Can target an already-artifact permanent (no-op but legal)")
    void canTargetAlreadyArtifact() {
        addReadyCoating(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(artifact)).isTrue();
    }

    @Test
    @DisplayName("Artifact type wears off at end of turn")
    void artifactTypeWearsOffAtEndOfTurn() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(target)).isTrue();

        // Advance through END_STEP to trigger CLEANUP which resets end-of-turn modifiers
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getGrantedCardTypes()).isEmpty();
        assertThat(gqs.isArtifact(target)).isFalse();
    }

    @Test
    @DisplayName("Activating taps Liquimetal Coating")
    void activatingTapsCoating() {
        Permanent coating = addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(coating.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent coating = addReadyCoating(player1);
        coating.tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Resolving ability logs the type change")
    void resolvingLogsTypeChange() {
        addReadyCoating(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("becomes an") && log.contains("Artifact"));
    }

    @Test
    @DisplayName("A noncreature Coating can activate on the turn it enters")
    void canActivateOnTurnItEnters() {
        Permanent coating = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(coating.isTapped()).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Coating can target itself")
    void canTargetItself() {
        Permanent coating = addReadyCoating(player1);

        harness.activateAbility(player1, 0, null, coating.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(coating.isTapped()).isTrue();
        assertThat(gqs.isArtifact(gd, coating)).isTrue();
    }

    @Test
    @DisplayName("The ability resolves after Coating leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent coating = addReadyCoating(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(coating);
        gd.playerGraveyards.get(player1.getId()).add(coating.getCard());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Coating adds artifact after an earlier Song of the Dryads type change")
    void addsArtifactAfterEarlierLandTypeOverride() {
        addReadyCoating(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isFalse();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isTrue();
    }

    private Permanent addReadyCoating(Player player) {
        return addCreatureReady(player, new LiquimetalCoating());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AngelsFeather());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GloriousAnthem());
    }
}
