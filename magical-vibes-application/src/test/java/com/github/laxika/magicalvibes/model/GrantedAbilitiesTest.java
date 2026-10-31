package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abilities granted at runtime are stored as {@link Ability} values and read back through the slot
 * views ({@link LegacySlots}).
 */
class GrantedAbilitiesTest {

    private static final CardEffect DRAW_ONE = new DrawCardEffect(1);
    private static final CardEffect DRAW_TWO = new DrawCardEffect(2);
    private static final CardEffect GAIN_ONE = new GainLifeEffect(1);

    @Test
    void aSlotAndAnEffectStandForTheAbilityOfTheirKind() {
        assertThat(LegacySlots.ability(EffectSlot.STATIC, DRAW_ONE)).isEqualTo(new StaticAbility(List.of(DRAW_ONE)));
        assertThat(LegacySlots.ability(EffectSlot.COMMAND_ZONE_STATIC, DRAW_ONE))
                .isEqualTo(new StaticAbility(List.of(DRAW_ONE), AbilityZones.COMMAND));
        Ability death = LegacySlots.ability(EffectSlot.ON_DEATH, DRAW_ONE);
        assertThat(death).isInstanceOf(TriggeredAbility.class);
        assertThat(((TriggeredAbility) death).conditions().getFirst().trigger())
                .isEqualTo(new LegacySlotTrigger(EffectSlot.ON_DEATH));
        for (EffectSlot slot : List.of(EffectSlot.STATIC, EffectSlot.COMMAND_ZONE_STATIC, EffectSlot.ON_DEATH)) {
            Ability ability = LegacySlots.ability(slot, DRAW_ONE);
            assertThat(LegacySlots.slotOf(ability)).isEqualTo(slot);
            assertThat(LegacySlots.effectsOf(ability)).containsExactly(DRAW_ONE);
        }
    }

    @Test
    void anOptionalGrantWithoutAnEffectIsNoAbility() {
        assertThat(LegacySlots.grant(null, null)).isNull();
        assertThat(LegacySlots.grant(EffectSlot.ON_DEATH, null)).isNull();
    }

    @Test
    void aSpellAbilityIsInNoSlot() {
        SpellAbility spell = new SpellAbility(List.of(new EffectRegistration(DRAW_ONE)));

        assertThat(LegacySlots.slotOf(spell)).isNull();
        assertThat(LegacySlots.effectsOf(spell)).containsExactly(DRAW_ONE);
    }

    @Test
    void slotViewsListOnlyTheirSlotsEffectsInOrder() {
        List<Ability> abilities = List.of(
                LegacySlots.ability(EffectSlot.ON_DEATH, DRAW_ONE),
                LegacySlots.ability(EffectSlot.STATIC, GAIN_ONE),
                LegacySlots.ability(EffectSlot.ON_DEATH, DRAW_TWO));

        assertThat(LegacySlots.effectsIn(abilities, EffectSlot.ON_DEATH)).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(LegacySlots.effectsIn(abilities, EffectSlot.STATIC)).containsExactly(GAIN_ONE);
        assertThat(LegacySlots.effectsIn(abilities, EffectSlot.ON_ATTACK)).isEmpty();
    }

    @Test
    void simultaneousRegistrationsBecomeOneAbility() {
        Map<EffectSlot, List<EffectRegistration>> registrations = new LinkedHashMap<>();
        registrations.put(EffectSlot.ON_DEATH, List.of(
                new EffectRegistration(DRAW_ONE, TriggerMode.INDEPENDENT),
                new EffectRegistration(DRAW_TWO),
                new EffectRegistration(GAIN_ONE, TriggerMode.NORMAL, true)));

        List<Ability> abilities = LegacySlots.registrationAbilities(registrations);

        assertThat(abilities).hasSize(2);
        assertThat(((TriggeredAbility) abilities.get(0)).effects())
                .containsExactly(new EffectRegistration(DRAW_ONE, TriggerMode.INDEPENDENT));
        assertThat(((TriggeredAbility) abilities.get(1)).effects()).containsExactly(
                new EffectRegistration(DRAW_TWO), new EffectRegistration(GAIN_ONE, TriggerMode.NORMAL, true));
    }

    @Test
    void anAddedAbilityJoinsTheCardsSlotViews() {
        Card card = new Card();
        card.addSpellEffect(DRAW_ONE);

        card.addAbility(new SpellAbility(List.of(new EffectRegistration(DRAW_TWO))));
        card.addAbility(LegacySlots.ability(EffectSlot.STATIC, GAIN_ONE));

        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(card.getAbilities()).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC)).containsExactly(GAIN_ONE);
    }

    @Test
    void aPermanentsGrantsAreReadBySlotAndExpireByDuration() {
        Permanent permanent = new Permanent(new Card());
        UUID expiryPlayer = UUID.randomUUID();
        permanent.addTemporaryTriggeredEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        permanent.addCombatTriggeredEffect(EffectSlot.ON_DEATH, DRAW_TWO);
        permanent.addUntilNextUpkeepTriggeredEffect(expiryPlayer, EffectSlot.ON_DEATH, GAIN_ONE);
        permanent.addPersistentTriggeredEffect(EffectSlot.UPKEEP_TRIGGERED, DRAW_ONE);

        assertThat(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH))
                .containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(permanent.getPersistentTriggeredEffects(EffectSlot.UPKEEP_TRIGGERED)).containsExactly(DRAW_ONE);

        permanent.clearUntilNextUpkeepTriggeredEffects(expiryPlayer);
        permanent.clearCombatState();

        assertThat(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).containsExactly(DRAW_ONE);
    }

    @Test
    void persistentGrantsCanBeRemovedBySlotAndEffect() {
        Permanent permanent = new Permanent(new Card());
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_DEATH, GAIN_ONE);
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_ATTACK, DRAW_ONE);

        permanent.removePersistentTriggeredEffectsIf(EffectSlot.ON_DEATH, DrawCardEffect.class::isInstance);

        assertThat(permanent.getPersistentTriggeredEffects(EffectSlot.ON_DEATH)).containsExactly(GAIN_ONE);
        assertThat(permanent.getPersistentTriggeredEffects(EffectSlot.ON_ATTACK)).containsExactly(DRAW_ONE);
    }

    @Test
    void aSpellCarriesTheAbilitiesItsPermanentGainsAsItEnters() {
        StackEntry entry = new StackEntry(new Card(), UUID.randomUUID());

        entry.addPersistentTriggeredEffectOnEntry(EffectSlot.ON_DEATH, DRAW_ONE);
        entry.addGrantedTriggeredEffectOnEntry(EffectSlot.ON_ATTACK, DRAW_TWO);

        assertThat(entry.getPersistentAbilitiesOnEntry())
                .containsExactly(LegacySlots.ability(EffectSlot.ON_DEATH, DRAW_ONE));
        assertThat(entry.getGrantedAbilitiesOnEntry())
                .containsExactly(LegacySlots.ability(EffectSlot.ON_ATTACK, DRAW_TWO));
        assertThatThrownBy(() -> entry.getGrantedAbilitiesOnEntry().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void repeatedPerpetualGrantsStackUnlessGrantedOnce() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();

        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_SELF_CAST, DRAW_ONE);
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_SELF_CAST, DRAW_ONE);
        gameData.grantPerpetualAbilityOnce(cardId, EffectSlot.ON_SELF_CAST, DRAW_ONE);
        gameData.grantPerpetualAbilityOnce(cardId, EffectSlot.ON_DEATH, GAIN_ONE);

        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_SELF_CAST))
                .containsExactly(DRAW_ONE, DRAW_ONE);
        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_DEATH)).containsExactly(GAIN_ONE);
    }

    @Test
    void removingTheLastPerpetualGrantForgetsTheCard() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_DEATH, DRAW_ONE);
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_ATTACK, DRAW_ONE);

        gameData.removePerpetualAbilitiesIf(cardId, EffectSlot.ON_DEATH, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_ATTACK)).containsExactly(DRAW_ONE);

        gameData.removePerpetualAbilitiesIf(cardId, EffectSlot.ON_ATTACK, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualTriggeredAbilityGrants).doesNotContainKey(cardId);
    }

    @Test
    void grantEffectsHoldTheAbilityTheirSlotAndEffectStandFor() {
        var grant = new GrantEffectToTargetEffect(EffectSlot.UPKEEP_TRIGGERED, DRAW_ONE);

        assertThat(grant.grantedAbility()).isEqualTo(LegacySlots.ability(EffectSlot.UPKEEP_TRIGGERED, DRAW_ONE));
        assertThat(grant.slot()).isEqualTo(EffectSlot.UPKEEP_TRIGGERED);
        assertThat(grant.grantedEffect()).isEqualTo(DRAW_ONE);

        var handGrant = new PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(EffectSlot.ON_SELF_CAST,
                List.of(DRAW_ONE, DRAW_TWO), new CardAllOfPredicate(List.of()));
        assertThat(handGrant.grantedAbilities()).hasSize(2);
        assertThat(handGrant.slot()).isEqualTo(EffectSlot.ON_SELF_CAST);
        assertThat(handGrant.grantedEffects()).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void aGlobalTriggerHoldsATriggeredAbility() {
        var global = new TemporaryGlobalTriggeredAbility(UUID.randomUUID(), new Card(), EffectSlot.ON_ANY_PLAYER_GAINS_LIFE,
                DRAW_ONE);

        assertThat(global.ability()).isEqualTo(LegacySlots.triggered(EffectSlot.ON_ANY_PLAYER_GAINS_LIFE, DRAW_ONE));
        assertThat(global.slot()).isEqualTo(EffectSlot.ON_ANY_PLAYER_GAINS_LIFE);
        assertThat(global.effect()).isEqualTo(DRAW_ONE);
    }
}
