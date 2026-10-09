package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DorotheaVengefulVictim.class, DorotheasRetribution.class, FountainOfYouth.class,
        GrizzlyBears.class, SorinTheMirthless.class})
class DorotheaVengefulVictimTest extends BaseCardTest {

    @Test
    @DisplayName("Dorothea is sacrificed at end of combat after attacking")
    void frontFaceIsSacrificedAfterAttacking() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dorothea);
    }

    @Test
    @DisplayName("Dorothea is sacrificed at end of combat after blocking")
    void frontFaceIsSacrificedAfterBlocking() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dorothea);
    }

    @Test
    @DisplayName("Disturb casts Dorothea's Retribution transformed and grants its attack trigger")
    void disturbCreatesTappedAttackingSpiritThatIsSacrificedAtEndOfCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getCard()).isInstanceOf(DorotheasRetribution.class);
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()).toList())
                .contains("A 4/4 Spirit creature token enters the battlefield tapped and attacking.");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))).isTrue();
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Dorothea's Retribution is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGoingToGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(aura.getOriginalCard().getId());
    }

    @Test
    void frontFaceRemainsWhenItNeitherAttacksNorBlocks() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());

        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dorothea);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void frontFaceSacrificeWaitsForItsDelayedTriggerToResolve() {
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(player1, List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dorothea);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dorothea);
        harness.assertInGraveyard(player1, "Dorothea, Vengeful Victim");
    }

    @Test
    void frontFaceCannotBeSacrificedByItsFormerController() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent dorothea = addCreatureReady(player1, new DorotheaVengefulVictim());
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(player1, List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });
        gd.playerBattlefields.get(player1.getId()).remove(dorothea);
        gd.playerBattlefields.get(player2.getId()).add(dorothea);
        gd.stolenCreatures.put(dorothea.getId(), player1.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control change", null,
                player2.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                dorothea.getId(), null, null, EffectDuration.PERMANENT, 0));

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dorothea);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void spiritEntersTappedAndAttackingBeforeCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castWithDisturb(creature);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.passBothPriorities();
        });

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.isTapped()).isTrue();
        assertThat(spirit.isAttacking()).isTrue();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 14);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void spiritSacrificeWaitsForItsDelayedTriggerToResolve() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castWithDisturb(creature);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(player1, List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void enchantingOpponentsCreatureGivesThatOpponentTheToken() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            harness.passBothPriorities();
        });

        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.assertLife(player1, 14);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    void spiritControllerChoosesBetweenDefendingPlayerAndPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castWithDisturb(creature);
        Permanent sorin = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        sorin.setCounterCount(CounterType.LOYALTY, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            harness.passBothPriorities();
        });

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void auraIsExiledWhenEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Dorothea, Vengeful Victim");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(aura.getOriginalCard().getId());
    }

    @Test
    void disturbSpellIsExiledIfItsCreatureTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        DorotheaVengefulVictim dorothea = new DorotheaVengefulVictim();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(dorothea));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dorothea's Retribution");
        harness.assertNotInGraveyard(player1, "Dorothea, Vengeful Victim");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(dorothea.getId());
    }

    @Test
    void disturbCannotBePaidWithOnlyTheFrontFaceManaCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInGraveyard(player1, "Dorothea, Vengeful Victim");
    }

    private Permanent castWithDisturb(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DorotheaVengefulVictim()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof DorotheaVengefulVictim)
                .findFirst()
                .orElseThrow();
    }
}
