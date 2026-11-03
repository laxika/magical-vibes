package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;
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

    private static CastAbility castAbility(SpellTrigger trigger, TriggerOptions options, boolean onStackPath,
                                           CardEffect... effects) {
        return CastAbility.of(new TriggeredAbility(trigger, options, effects), trigger.filter(), onStackPath);
    }

    @Test
    void aFilteredAbilityIsItsFilterEffectsAndInterveningIf() {
        SpellTrigger creatureSpell = Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                .duringOpponentsTurn();

        CastAbility cast = castAbility(creatureSpell,
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()), true, DRAW, GAIN);

        assertThat(cast).isEqualTo(new CastAbility(creatureSpell.filter(), List.of(DRAW, GAIN),
                new SourceIsEnchantment(), null, null, null));
    }

    @Test
    void aLonePlainYouMayIsTheOptionalPartOnTheStackPathOnly() {
        SpellTrigger anySpell = Triggers.youCastSpell().anySpell();
        CardEffect drawAndGain = SequenceEffect.of(DRAW, GAIN);
        MayEffect may = new MayEffect(drawAndGain, "Draw?");

        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, true, may))
                .isEqualTo(new CastAbility(SpellFilter.ANY, List.of(drawAndGain), null, null, "Draw?", null));
        // The graveyard, hand and command-zone readers take the "you may" as an effect.
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, false, may))
                .isEqualTo(new CastAbility(SpellFilter.ANY, List.of(may), null, null, null, null));
        // Another player's choice isn't the ability's optional part.
        MayEffect theirChoice = new MayEffect(DRAW, "Draw?", null, MayChoicePlayer.TRIGGERING_SPELL_CONTROLLER);
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, true, theirChoice).effects())
                .containsExactly(theirChoice);
    }

    @Test
    void aLonePlainYouMayPayIsTheCostAndOnTheStackPathThePrompt() {
        SpellTrigger anySpell = Triggers.opponentCastsSpell().anySpell();
        MayPayManaEffect pay = new MayPayManaEffect("{1}", DRAW, "Pay {1}?");

        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, true, pay))
                .isEqualTo(new CastAbility(SpellFilter.ANY, List.of(DRAW), null, "{1}", "Pay {1}?", null));
        assertThat(castAbility(anySpell, TriggerOptions.DEFAULT, false, pay))
                .isEqualTo(new CastAbility(SpellFilter.ANY, List.of(DRAW), null, "{1}", null, null));
    }

    @Test
    void anAuthoredRecordIsJudgedAsTheFilterItsFieldsSay() {
        var creature = new CardTypePredicate(CardType.CREATURE);
        var anyPermanent = TargetFilters.permanent();
        SpellCastTriggerEffect authored = new SpellCastTriggerEffect(creature, List.of(DRAW), "{2}", anyPermanent, new StackEntryKickedPredicate(), false, true, new SourceIsEnchantment(), 2,
                0, false, true, 0, false, false, false, true, Zone.GRAVEYARD, null, true, true, true);

        CastAbility cast = CastAbility.of(authored, new MayEffect(authored, "Pay {2}?"));

        assertThat(cast).isEqualTo(new CastAbility(SpellFilter.ANY.withSpell(creature)
                .withStackEntry(new StackEntryKickedPredicate()).withTurn(SpellFilter.Turn.YOURS).withDuringCombat()
                .withNthThisTurn(2).withPaidWithManaFromThis().withCastFrom(Zone.GRAVEYARD)
                .withKicker(SpellFilter.Kicker.EACH_KICK).withEveryPlayersSpells(),
                List.of(DRAW), new SourceIsEnchantment(), "{2}", "Pay {2}?", anyPermanent));
        assertThat(CastAbility.of(authored, null).optionalPrompt()).isNull();
    }
}
