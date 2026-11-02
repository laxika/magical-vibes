package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
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
    void aSlotAndAnEffectStandForATriggeredAbility() {
        Ability untap = LegacySlots.ability(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);
        assertThat(untap).isInstanceOf(TriggeredAbility.class);
        assertThat(((TriggeredAbility) untap).conditions().getFirst().trigger())
                .isEqualTo(new LegacySlotTrigger(EffectSlot.ON_SELF_BECOMES_UNTAPPED));
        assertThat(LegacySlots.slotOf(untap)).isEqualTo(EffectSlot.ON_SELF_BECOMES_UNTAPPED);
        assertThat(LegacySlots.effectsOf(untap)).containsExactly(DRAW_ONE);
    }

    @Test
    void aStaticAbilityIsInNoSlot() {
        StaticAbility statik = new StaticAbility(List.of(DRAW_ONE), AbilityZones.COMMAND);

        assertThat(LegacySlots.slotOf(statik)).isNull();
        assertThat(LegacySlots.effectsOf(statik)).containsExactly(DRAW_ONE);
        assertThat(LegacySlots.label(statik)).isEqualTo("STATIC");
        assertThat(LegacySlots.withEffect(statik, GAIN_ONE))
                .isEqualTo(new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND));
    }

    @Test
    void anOptionalGrantWithoutAnEffectIsNoAbility() {
        assertThat(LegacySlots.grant(null, null)).isNull();
        assertThat(LegacySlots.grant(EffectSlot.ON_SELF_BECOMES_UNTAPPED, null)).isNull();
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
                LegacySlots.ability(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE),
                StaticAbility.of(GAIN_ONE),
                LegacySlots.ability(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_TWO));

        assertThat(LegacySlots.effectsIn(abilities, EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(LegacySlots.staticEffectsIn(abilities)).containsExactly(GAIN_ONE);
        assertThat(LegacySlots.effectsIn(abilities, EffectSlot.ON_CONTROLLER_CLASHES)).isEmpty();
    }

    @Test
    void simultaneousRegistrationsBecomeOneAbility() {
        Map<EffectSlot, List<EffectRegistration>> registrations = new LinkedHashMap<>();
        registrations.put(EffectSlot.ON_SELF_BECOMES_UNTAPPED, List.of(
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
        card.addAbility(StaticAbility.of(GAIN_ONE));

        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(card.getAbilities()).hasSize(2);
        assertThat(card.getStaticEffects()).containsExactly(GAIN_ONE);
    }

    @Test
    void aPermanentsGrantsAreReadBySlotAndExpireByDuration() {
        Permanent permanent = new Permanent(new Card());
        UUID expiryPlayer = UUID.randomUUID();
        permanent.addTemporaryTriggeredEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);
        permanent.addCombatTriggeredEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_TWO);
        permanent.addUntilNextUpkeepTriggeredEffect(expiryPlayer, EffectSlot.ON_SELF_BECOMES_UNTAPPED, GAIN_ONE);
        permanent.addPersistentAbility(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), DRAW_ONE));

        assertThat(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED))
                .containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(permanent.getPersistentTriggerEffects(Triggers.upkeep(ActivePlayer.YOU))).containsExactly(DRAW_ONE);

        permanent.clearUntilNextUpkeepTriggeredEffects(expiryPlayer);
        permanent.clearCombatState();

        assertThat(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(DRAW_ONE);
    }

    @Test
    void aPermanentsStaticGrantsAreReadApartFromItsTriggers() {
        Permanent permanent = new Permanent(new Card());
        UUID expiryPlayer = UUID.randomUUID();
        permanent.addTemporaryAbility(StaticAbility.of(DRAW_ONE));
        permanent.addCombatAbility(StaticAbility.of(DRAW_TWO));
        permanent.addUntilNextUpkeepAbility(expiryPlayer, StaticAbility.of(GAIN_ONE));
        permanent.addPersistentAbility(StaticAbility.of(DRAW_ONE));
        permanent.addPersistentAbility(StaticAbility.of(GAIN_ONE));
        permanent.addTemporaryTriggeredEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, GAIN_ONE);

        assertThat(permanent.getTemporaryStaticEffects()).containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(permanent.getPersistentStaticEffects()).containsExactly(DRAW_ONE, GAIN_ONE);
        assertThat(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(GAIN_ONE);

        permanent.removePersistentStaticEffectsIf(GAIN_ONE::equals);
        permanent.clearUntilNextUpkeepTriggeredEffects(expiryPlayer);
        permanent.clearCombatState();

        assertThat(permanent.getPersistentStaticEffects()).containsExactly(DRAW_ONE);
        assertThat(permanent.getTemporaryStaticEffects()).containsExactly(DRAW_ONE);
    }

    @Test
    void perpetualStaticGrantsAreReadApartFromTriggers() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, StaticAbility.of(DRAW_ONE));
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED, GAIN_ONE);
        gameData.grantPerpetualAbility(cardId, StaticAbility.of(DRAW_ONE));

        assertThat(gameData.perpetualStaticEffects(cardId)).containsExactly(DRAW_ONE, DRAW_ONE);
        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(GAIN_ONE);
    }

    @Test
    void persistentGrantsCanBeRemovedBySlotAndEffect() {
        Permanent permanent = new Permanent(new Card());
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, GAIN_ONE);
        permanent.addPersistentTriggeredEffect(EffectSlot.ON_CONTROLLER_CLASHES, DRAW_ONE);

        permanent.removePersistentTriggeredEffectsIf(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DrawCardEffect.class::isInstance);

        assertThat(permanent.getPersistentTriggeredEffects(EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(GAIN_ONE);
        assertThat(permanent.getPersistentTriggeredEffects(EffectSlot.ON_CONTROLLER_CLASHES)).containsExactly(DRAW_ONE);
    }

    @Test
    void aSpellCarriesTheAbilitiesItsPermanentGainsAsItEnters() {
        StackEntry entry = new StackEntry(new Card(), UUID.randomUUID());

        entry.addPersistentTriggeredEffectOnEntry(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);
        entry.addGrantedTriggeredEffectOnEntry(EffectSlot.ON_CONTROLLER_CLASHES, DRAW_TWO);

        assertThat(entry.getPersistentAbilitiesOnEntry())
                .containsExactly(LegacySlots.ability(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE));
        assertThat(entry.getGrantedAbilitiesOnEntry())
                .containsExactly(LegacySlots.ability(EffectSlot.ON_CONTROLLER_CLASHES, DRAW_TWO));
        assertThatThrownBy(() -> entry.getGrantedAbilitiesOnEntry().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void repeatedPerpetualGrantsStackUnlessGrantedOnce() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();

        TriggeredAbility castDraw = new TriggeredAbility(Triggers.youCastThisSpell(), DRAW_ONE);
        gameData.grantPerpetualAbility(cardId, castDraw);
        gameData.grantPerpetualAbility(cardId, castDraw);
        gameData.grantPerpetualAbilityOnce(cardId, castDraw);
        gameData.grantPerpetualAbilityOnce(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED, GAIN_ONE);

        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.youCastThisSpell(), Zone.STACK))
                .containsExactly(DRAW_ONE, DRAW_ONE);
        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED)).containsExactly(GAIN_ONE);
    }

    @Test
    void removingTheLastPerpetualGrantForgetsTheCard() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);
        gameData.grantPerpetualAbility(cardId, EffectSlot.ON_CONTROLLER_CLASHES, DRAW_ONE);

        gameData.removePerpetualAbilitiesIf(cardId, EffectSlot.ON_SELF_BECOMES_UNTAPPED, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualAbilityEffects(cardId, EffectSlot.ON_CONTROLLER_CLASHES)).containsExactly(DRAW_ONE);

        gameData.removePerpetualAbilitiesIf(cardId, EffectSlot.ON_CONTROLLER_CLASHES, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualTriggeredAbilityGrants).doesNotContainKey(cardId);
    }

    @Test
    void grantEffectsHoldTheAbilityTheirSlotAndEffectStandFor() {
        var grant = new GrantEffectToTargetEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE);

        assertThat(grant.grantedAbility()).isEqualTo(LegacySlots.ability(EffectSlot.ON_SELF_BECOMES_UNTAPPED, DRAW_ONE));
        assertThat(grant.slot()).isEqualTo(EffectSlot.ON_SELF_BECOMES_UNTAPPED);
        assertThat(grant.grantedEffect()).isEqualTo(DRAW_ONE);

        var handGrant = new PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(Triggers.youCastThisSpell(),
                List.of(DRAW_ONE, DRAW_TWO), new CardAllOfPredicate(List.of()));
        assertThat(handGrant.grantedAbilities()).containsExactly(
                new TriggeredAbility(Triggers.youCastThisSpell(), DRAW_ONE),
                new TriggeredAbility(Triggers.youCastThisSpell(), DRAW_TWO));
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
