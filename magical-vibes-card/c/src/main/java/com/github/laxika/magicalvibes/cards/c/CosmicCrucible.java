package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "MSC", collectorNumber = "79")
@CardRegistration(set = "MSC", collectorNumber = "397")
public class CosmicCrucible extends Card {

    public CosmicCrucible() {
        addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), new AwardAnyColorManaEffect(4, true));
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new MayEffect(new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens(), "Copy that spell?"));
    }
}
