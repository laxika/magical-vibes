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
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Abilities granted at runtime are stored as {@link Ability} values and read back by trigger, and how
 * {@link LegacySlots} treats abilities that no slot holds.
 */
class GrantedAbilitiesTest {

    private static final Trigger LOSES_CONTROL = Triggers.youLoseControlOfThis();
    private static final Trigger GAINS_CONTROL = Triggers.youGainControlOfThis();

    private static final CardEffect DRAW_ONE = new DrawCardEffect(1);
    private static final CardEffect DRAW_TWO = new DrawCardEffect(2);
    private static final CardEffect GAIN_ONE = new GainLifeEffect(1);

    @Test
    void aTriggeredAbilityWithARealTriggerIsInNoSlot() {
        Ability ability = new TriggeredAbility(LOSES_CONTROL, DRAW_ONE);

        assertThat(LegacySlots.slotOf(ability)).isNull();
        assertThat(LegacySlots.effectsOf(ability)).containsExactly(DRAW_ONE);
        assertThat(LegacySlots.label(ability)).isEqualTo("TRIGGERED");
        assertThat(LegacySlots.withEffect(ability, GAIN_ONE)).isEqualTo(new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));
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
    }

    @Test
    void aSpellAbilityIsInNoSlot() {
        SpellAbility spell = new SpellAbility(List.of(new EffectRegistration(DRAW_ONE)));

        assertThat(LegacySlots.slotOf(spell)).isNull();
        assertThat(LegacySlots.effectsOf(spell)).containsExactly(DRAW_ONE);
    }

    @Test
    void theStaticViewListsOnlyStaticEffectsInOrder() {
        List<Ability> abilities = List.of(
                new TriggeredAbility(LOSES_CONTROL, DRAW_ONE),
                StaticAbility.of(GAIN_ONE),
                StaticAbility.of(DRAW_TWO));

        assertThat(LegacySlots.staticEffectsIn(abilities)).containsExactly(GAIN_ONE, DRAW_TWO);
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
    void aPermanentsGrantsAreReadByTriggerAndExpireByDuration() {
        Permanent permanent = new Permanent(new Card());
        UUID expiryPlayer = UUID.randomUUID();
        permanent.addTemporaryAbility(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        permanent.addCombatAbility(new TriggeredAbility(LOSES_CONTROL, DRAW_TWO));
        permanent.addUntilNextUpkeepAbility(expiryPlayer, new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));
        permanent.addPersistentAbility(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), DRAW_ONE));

        assertThat(permanent.getTemporaryTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(permanent.getPersistentTriggerEffects(Triggers.upkeep(ActivePlayer.YOU))).containsExactly(DRAW_ONE);

        permanent.clearUntilNextUpkeepTriggeredEffects(expiryPlayer);
        permanent.clearCombatState();

        assertThat(permanent.getTemporaryTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE);
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
        permanent.addTemporaryAbility(new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));

        assertThat(permanent.getTemporaryStaticEffects()).containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(permanent.getPersistentStaticEffects()).containsExactly(DRAW_ONE, GAIN_ONE);
        assertThat(permanent.getTemporaryTriggerEffects(LOSES_CONTROL)).containsExactly(GAIN_ONE);

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
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));
        gameData.grantPerpetualAbility(cardId, StaticAbility.of(DRAW_ONE));

        assertThat(gameData.perpetualStaticEffects(cardId)).containsExactly(DRAW_ONE, DRAW_ONE);
        assertThat(gameData.perpetualTriggerEffects(cardId, LOSES_CONTROL, Zone.BATTLEFIELD)).containsExactly(GAIN_ONE);
    }

    @Test
    void persistentGrantsCanBeRemovedByTriggerAndEffect() {
        Permanent permanent = new Permanent(new Card());
        permanent.addPersistentAbility(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        permanent.addPersistentAbility(new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));
        permanent.addPersistentAbility(new TriggeredAbility(GAINS_CONTROL, DRAW_ONE));

        permanent.removePersistentTriggeredEffectsIf(LOSES_CONTROL, DrawCardEffect.class::isInstance);

        assertThat(permanent.getPersistentTriggerEffects(LOSES_CONTROL)).containsExactly(GAIN_ONE);
        assertThat(permanent.getPersistentTriggerEffects(GAINS_CONTROL)).containsExactly(DRAW_ONE);
    }

    @Test
    void aSpellCarriesTheAbilitiesItsPermanentGainsAsItEnters() {
        StackEntry entry = new StackEntry(new Card(), UUID.randomUUID());

        entry.addPersistentAbilityOnEntry(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));

        assertThat(entry.getPersistentAbilitiesOnEntry()).containsExactly(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        assertThat(entry.getGrantedAbilitiesOnEntry()).isEmpty();
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
        gameData.grantPerpetualAbilityOnce(cardId, new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));

        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.youCastThisSpell(), Zone.STACK))
                .containsExactly(DRAW_ONE, DRAW_ONE);
        assertThat(gameData.perpetualTriggerEffects(cardId, LOSES_CONTROL, Zone.BATTLEFIELD)).containsExactly(GAIN_ONE);
    }

    @Test
    void removingTheLastPerpetualGrantForgetsTheCard() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(GAINS_CONTROL, DRAW_ONE));

        gameData.removePerpetualAbilitiesIf(cardId, LOSES_CONTROL, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualTriggerEffects(cardId, GAINS_CONTROL, Zone.BATTLEFIELD)).containsExactly(DRAW_ONE);

        gameData.removePerpetualAbilitiesIf(cardId, GAINS_CONTROL, DrawCardEffect.class::isInstance);
        assertThat(gameData.perpetualTriggeredAbilityGrants).doesNotContainKey(cardId);
    }

    @Test
    void grantEffectsHoldTheAbilityTheyGrant() {
        var grant = new GrantEffectToTargetEffect(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));

        assertThat(grant.grantedAbility()).isEqualTo(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        assertThat(grant.grantedEffect()).isEqualTo(DRAW_ONE);

        var handGrant = new PerpetuallyGrantTriggeredAbilityToTargetCardInHandEffect(Triggers.youCastThisSpell(),
                List.of(DRAW_ONE, DRAW_TWO), new CardAllOfPredicate(List.of()));
        assertThat(handGrant.grantedAbilities()).containsExactly(
                new TriggeredAbility(Triggers.youCastThisSpell(), DRAW_ONE, DRAW_TWO));
        assertThat(handGrant.grantedEffects()).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void aGlobalTriggerHoldsATriggeredAbility() {
        TriggeredAbility ability = new TriggeredAbility(LOSES_CONTROL, DRAW_ONE);
        var global = new TemporaryGlobalTriggeredAbility(UUID.randomUUID(), new Card(), ability);

        assertThat(global.ability()).isEqualTo(ability);
    }
}
