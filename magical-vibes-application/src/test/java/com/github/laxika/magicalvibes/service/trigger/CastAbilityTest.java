package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.SpellFilter;
import com.github.laxika.magicalvibes.model.trigger.SpellTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CastAbilityTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    private static CastAbility castAbility(SpellTrigger trigger, TriggerOptions options, CardEffect... effects) {
        return CastAbility.of(new TriggeredAbility(trigger, options, effects), trigger.filter());
    }

    @Test
    void aFilteredAbilityIsItsFilterEffectsAndInterveningIf() {
        SpellTrigger creatureSpell = Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                .duringOpponentsTurn();

        CastAbility cast = castAbility(creatureSpell,
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()), DRAW, GAIN);

        assertThat(cast).isEqualTo(new CastAbility(creatureSpell.filter(), List.of(DRAW, GAIN),
                new SourceIsEnchantment(), null));
    }

    @Test
    void theAbilitysOwnTargetFilterIsItsTargetFilter() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();
        TriggeredAbility emblem = new TriggeredAbility(anySpell, DRAW).withTargetFilter(TargetFilters.creature());

        CastAbility cast = CastAbility.of(emblem, anySpell.filter());

        assertThat(cast.targetFilter()).isEqualTo(TargetFilters.creature());
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, DRAW).targetFilter()).isNull();
    }

    @Test
    void anEventConditionIsCarriedApartFromTheInterveningIf() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();

        CastAbility cast = castAbility(anySpell,
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsEnchantment()), DRAW);

        assertThat(cast).isEqualTo(new CastAbility(SpellFilter.ANY, List.of(DRAW), null, null,
                new SourceIsEnchantment()));
        assertThat(cast.interveningIf()).isNull();
    }

    @Test
    void aLonePlainYouMayStaysAnEffectAndItsTargetsAreWhatItOffers() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();
        CardEffect drawAndGain = SequenceEffect.of(DRAW, GAIN);
        MayEffect may = new MayEffect(drawAndGain, "Draw?");

        CastAbility cast = castAbility(anySpell, TriggerOptions.DEFAULT, may);

        assertThat(cast.effects()).containsExactly(may);
        assertThat(cast.effectsTargetsAreChosenFor()).containsExactly(drawAndGain);
        // Another player's choice is judged as it is.
        MayEffect theirChoice = new MayEffect(DRAW, "Draw?", null, MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER);
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, theirChoice).effectsTargetsAreChosenFor())
                .containsExactly(theirChoice);
    }

    @Test
    void aLonePlainYouMayPayStaysAnEffectAndItsTargetsAreWhatItOffers() {
        SpellTrigger anySpell = Triggers.opponentCastsSpell().anySpell();
        MayPayManaEffect pay = new MayPayManaEffect("{1}", DRAW, "Pay {1}?");

        CastAbility cast = castAbility(anySpell, TriggerOptions.DEFAULT, pay);

        assertThat(cast.effects()).containsExactly(pay);
        assertThat(cast.effectsTargetsAreChosenFor()).containsExactly(DRAW);
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, pay, GAIN).effectsTargetsAreChosenFor())
                .containsExactly(pay, GAIN);
    }
}
