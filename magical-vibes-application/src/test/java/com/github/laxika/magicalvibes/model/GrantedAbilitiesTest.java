package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
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
 * {@link AbilityEffects} reads and adjusts each kind of ability.
 */
class GrantedAbilitiesTest {

    private static final Trigger LOSES_CONTROL = Triggers.youLoseControlOfThis();
    private static final Trigger GAINS_CONTROL = Triggers.youGainControlOfThis();

    private static final CardEffect DRAW_ONE = new DrawCardEffect(1);
    private static final CardEffect DRAW_TWO = new DrawCardEffect(2);
    private static final CardEffect GAIN_ONE = new GainLifeEffect(1);

    @Test
    void aTriggeredAbilityKeepsItsTriggerWithAnotherEffect() {
        Ability ability = new TriggeredAbility(LOSES_CONTROL, DRAW_ONE);

        assertThat(AbilityEffects.effectsOf(ability)).containsExactly(DRAW_ONE);
        assertThat(AbilityEffects.label(ability)).isEqualTo("TRIGGERED");
        assertThat(AbilityEffects.withEffect(ability, GAIN_ONE)).isEqualTo(new TriggeredAbility(LOSES_CONTROL, GAIN_ONE));
    }

    @Test
    void aStaticAbilityKeepsItsZonesWithAnotherEffect() {
        StaticAbility statik = new StaticAbility(List.of(DRAW_ONE), AbilityZones.COMMAND);

        assertThat(AbilityEffects.effectsOf(statik)).containsExactly(DRAW_ONE);
        assertThat(AbilityEffects.label(statik)).isEqualTo("STATIC");
        assertThat(AbilityEffects.withEffect(statik, GAIN_ONE))
                .isEqualTo(new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND));
    }

    @Test
    void aSpellAbilitysEffectsAreReadInOrder() {
        SpellAbility spell = new SpellAbility(List.of(new EffectRegistration(DRAW_ONE)));

        assertThat(AbilityEffects.effectsOf(spell)).containsExactly(DRAW_ONE);
    }

    @Test
    void theStaticViewListsOnlyStaticEffectsInOrder() {
        List<Ability> abilities = List.of(
                new TriggeredAbility(LOSES_CONTROL, DRAW_ONE),
                StaticAbility.of(GAIN_ONE),
                StaticAbility.of(DRAW_TWO));

        assertThat(AbilityEffects.staticEffectsIn(abilities)).containsExactly(GAIN_ONE, DRAW_TWO);
    }

    @Test
    void anAddedAbilityJoinsTheCardsAbilities() {
        Card card = new Card();
        card.addSpellEffect(DRAW_ONE);

        card.addAbility(new SpellAbility(List.of(new EffectRegistration(DRAW_TWO))));
        card.addAbility(StaticAbility.of(GAIN_ONE));

        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(card.getAbilities()).hasSize(2);
        assertThat(card.getStaticEffects()).containsExactly(GAIN_ONE);
    }

    @Test
    @DisplayName("A granted ability becomes its layer-6 grants: one for a triggered ability, one per effect of a static one")
    void aGrantedAbilityBecomesItsLayerSixGrants() {
        TriggeredAbility triggered = new TriggeredAbility(LOSES_CONTROL, DRAW_ONE);
        ActivatedAbility activated = new ActivatedAbility(true, null, List.of(DRAW_TWO), "{T}: Draw two cards.");

        assertThat(GameData.abilityGrantEffects(triggered, Duration.UNTIL_END_OF_TURN))
                .containsExactly(new GrantTriggeredAbilityEffect(triggered, Subjects.target()));
        assertThat(GameData.abilityGrantEffects(new StaticAbility(List.of(DRAW_ONE, GAIN_ONE), AbilityZones.BATTLEFIELD),
                Duration.PERMANENT)).containsExactly(new GrantEffectEffect(DRAW_ONE, Subjects.target()),
                new GrantEffectEffect(GAIN_ONE, Subjects.target()));
        assertThat(GameData.abilityGrantEffects(activated, Duration.UNTIL_YOUR_NEXT_TURN)).containsExactly(
                new GrantActivatedAbilityEffect(activated, Subjects.target(), null, Duration.UNTIL_YOUR_NEXT_TURN));
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
        assertThat(gameData.perpetualAbilityGrants).doesNotContainKey(cardId);
    }

    @Test
    void grantEffectsHoldTheAbilityTheyGrant() {
        var grant = new GrantEffectToTargetEffect(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));

        assertThat(grant.grantedAbility()).isEqualTo(new TriggeredAbility(LOSES_CONTROL, DRAW_ONE));
        assertThat(grant.grantedEffect()).isEqualTo(DRAW_ONE);
    }

    @Test
    void aGlobalTriggerHoldsATriggeredAbility() {
        TriggeredAbility ability = new TriggeredAbility(LOSES_CONTROL, DRAW_ONE);
        var global = new TemporaryGlobalTriggeredAbility(UUID.randomUUID(), new Card(), ability);

        assertThat(global.ability()).isEqualTo(ability);
    }
}
