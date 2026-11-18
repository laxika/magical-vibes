package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.AllowTargetCreatureToAdaptEffect;
import com.github.laxika.magicalvibes.model.effect.AllowTargetPlayerToTargetSourceIgnoringShroudEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceAuraEffectsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.IgnoreSourceDampingEngineEffectUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameDataPermanentEffectTest {

    private GameData gd;
    private final UUID creatureId = UUID.randomUUID();
    private final UUID otherCreatureId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        gd = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "Player1");
    }

    private FloatingContinuousEffect on(UUID permanentId, CardEffect effect, Duration duration) {
        return gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, UUID.randomUUID(), effect,
                permanentId, duration));
    }

    @Test
    @DisplayName("An effect a resolution makes on a permanent is found on that permanent only")
    void effectOnPermanentAppliesToThatPermanent() {
        StackEntry entry = new StackEntry(new Card(), UUID.randomUUID());

        gd.addEffectOnPermanent(entry, new CantBlockEffect(), creatureId, Duration.UNTIL_END_OF_TURN);

        assertThat(gd.hasFloatingEffectOnPermanent(creatureId, CantBlockEffect.class)).isTrue();
        assertThat(gd.hasFloatingEffectOnPermanent(otherCreatureId, CantBlockEffect.class)).isFalse();
        assertThat(gd.floatingEffectsOnPermanent(creatureId, CantBlockEffect.class).getFirst().controllerId())
                .isEqualTo(entry.getControllerId());
    }

    @Test
    @DisplayName("The newest requirement that names a player is the one the creature must attack")
    void newestNamedDefenderWins() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertThat(gd.mustAttackDefender(creatureId)).isNull();
        on(creatureId, new MustAttackIfAbleEffect(first), Duration.UNTIL_END_OF_TURN);
        on(creatureId, new MustAttackIfAbleEffect(second), Duration.UNTIL_END_OF_TURN);
        on(creatureId, new MustAttackIfAbleEffect(), Duration.UNTIL_END_OF_TURN);

        assertThat(gd.mustAttackDefender(creatureId)).isEqualTo(second);
    }

    @Test
    @DisplayName("Additional blocks add up, and \"any number\" overrides the count")
    void additionalBlocksAddUp() {
        on(creatureId, new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(1), Duration.UNTIL_END_OF_TURN);
        on(creatureId, new GrantAdditionalBlockToTargetUntilEndOfTurnEffect(2), Duration.UNTIL_END_OF_TURN);
        assertThat(gd.additionalBlocksThisTurn(creatureId)).isEqualTo(3);

        on(creatureId, new CanBlockAnyNumberOfCreaturesUntilEndOfTurnEffect(), Duration.UNTIL_END_OF_TURN);
        assertThat(gd.additionalBlocksThisTurn(creatureId)).isEqualTo(Integer.MAX_VALUE);
        assertThat(gd.additionalBlocksThisTurn(otherCreatureId)).isZero();
    }

    @Test
    @DisplayName("Turn-long and combat-long block requirements both name the attackers to block")
    void requiredBlockSourcesCoverBothDurations() {
        UUID turnAttacker = UUID.randomUUID();
        UUID combatAttacker = UUID.randomUUID();
        on(creatureId, new MustBlockSourceEffect(turnAttacker), Duration.UNTIL_END_OF_TURN);
        FloatingContinuousEffect combatLong = on(creatureId, new MustBlockSourceEffect(combatAttacker, null, true),
                Duration.UNTIL_END_OF_COMBAT);

        assertThat(gd.requiredBlockSourceIds(creatureId)).containsExactly(turnAttacker, combatAttacker);

        gd.endFloatingEffect(combatLong);
        assertThat(gd.requiredBlockSourceIds(creatureId)).containsExactly(turnAttacker);
    }

    @Test
    @DisplayName("The attackers a creature can't block are those its effects name")
    void cantBlockSourcesAreNamed() {
        UUID attacker = UUID.randomUUID();
        on(creatureId, new CantBlockSourceEffect(attacker), Duration.UNTIL_END_OF_TURN);

        assertThat(gd.cantBlockSourceIds(creatureId)).containsExactly(attacker);
        assertThat(gd.cantBlockSourceIds(otherCreatureId)).isEmpty();
    }

    @Test
    @DisplayName("Only the player who paid ignores the permanent's effect")
    void onlyThePayingPlayerIgnoresTheEffect() {
        UUID payer = UUID.randomUUID();
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, payer,
                new IgnoreSourceAuraEffectsUntilEndOfTurnEffect(), creatureId, Duration.UNTIL_END_OF_TURN));

        assertThat(gd.ignoresEffectOf(creatureId, IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class, payer)).isTrue();
        assertThat(gd.ignoresEffectOf(creatureId, IgnoreSourceAuraEffectsUntilEndOfTurnEffect.class,
                UUID.randomUUID())).isFalse();
        assertThat(gd.ignoresEffectOf(creatureId, IgnoreSourceDampingEngineEffectUntilEndOfTurnEffect.class, payer))
                .isFalse();
    }

    @Test
    @DisplayName("Only the named player may target the permanent as though it didn't have shroud")
    void shroudIsIgnoredOnlyForTheNamedPlayer() {
        UUID named = UUID.randomUUID();
        on(creatureId, new AllowTargetPlayerToTargetSourceIgnoringShroudEffect(named), Duration.UNTIL_END_OF_TURN);

        assertThat(gd.mayTargetIgnoringShroud(creatureId, named)).isTrue();
        assertThat(gd.mayTargetIgnoringShroud(creatureId, UUID.randomUUID())).isFalse();
        assertThat(gd.mayTargetIgnoringShroud(otherCreatureId, named)).isFalse();
    }

    @Test
    @DisplayName("Each Adapt override is used up once")
    void adaptOverrideIsUsedUp() {
        on(creatureId, new AllowTargetCreatureToAdaptEffect(), Duration.UNTIL_END_OF_TURN);

        assertThat(gd.useAdaptOverride(creatureId)).isTrue();
        assertThat(gd.useAdaptOverride(creatureId)).isFalse();
    }

    @Test
    @DisplayName("A permanent's prevention shields are used oldest first, and a partly used one keeps its timestamp")
    void permanentPreventionShieldsAreUsedOldestFirst() {
        FloatingContinuousEffect older = gd.addPermanentDamagePreventionShield(creatureId, 2);
        gd.addPermanentDamagePreventionShield(creatureId, 3);

        assertThat(gd.usePermanentDamagePreventionShields(creatureId, false, 3)).isEqualTo(3);

        assertThat(gd.permanentDamagePreventionShield(creatureId)).isEqualTo(2);
        assertThat(gd.floatingEffectsOnPermanent(creatureId, DamagePreventionShield.class))
                .singleElement()
                .satisfies(left -> assertThat(left.timestamp()).isGreaterThan(older.timestamp()));
        gd.addPermanentDamagePreventionShield("Test", null, null, creatureId, 4, false);
        FloatingContinuousEffect partlyUsed = gd.floatingEffectsOnPermanent(creatureId, DamagePreventionShield.class)
                .getFirst();
        gd.usePermanentDamagePreventionShields(creatureId, false, 1);
        assertThat(gd.floatingEffectsOnPermanent(creatureId, DamagePreventionShield.class).getFirst().timestamp())
                .isEqualTo(partlyUsed.timestamp());
    }

    @Test
    @DisplayName("A permanent's prevention shields are neither a player's nor the shared shield")
    void permanentPreventionShieldsAreKeptApartFromPlayerShields() {
        gd.addPermanentDamagePreventionShield(creatureId, 2);
        gd.addPermanentDamagePreventionShield("Temper", null, null, creatureId, 5, true);

        assertThat(gd.globalDamagePreventionShield()).isZero();
        assertThat(gd.usePlayerDamagePreventionShields(null, false, 2)).isZero();
        assertThat(gd.playerDamagePreventionShields()).isEmpty();
        assertThat(gd.permanentDamagePreventionShield(creatureId)).isEqualTo(2);
        assertThat(gd.permanentDamagePreventionShield(creatureId, true)).isEqualTo(5);
        assertThat(gd.permanentDamagePreventionShield(otherCreatureId)).isZero();
    }

    @Test
    @DisplayName("Each regeneration shield is its own effect on the permanent, used up one at a time")
    void regenerationShieldsAreUsedOneAtATime() {
        gd.addRegenerationShields(creatureId, 2);

        assertThat(gd.regenerationShieldCount(creatureId)).isEqualTo(2);
        assertThat(gd.useOldestEffectOnPermanent(creatureId, RegenerationShield.class)).isTrue();
        assertThat(gd.regenerationShieldCount(creatureId)).isEqualTo(1);
        assertThat(gd.regenerationShieldCount(otherCreatureId)).isZero();
        assertThat(gd.useOldestEffectOnPermanent(otherCreatureId, RegenerationShield.class)).isFalse();
    }
}
