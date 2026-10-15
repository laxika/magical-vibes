package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "REX", collectorNumber = "12")
@CardRegistration(set = "REX", collectorNumber = "37")
public class HenryWuInGenGeneticist extends Card {

    public HenryWuInGenGeneticist() {
        PermanentHasSubtypePredicate human = new PermanentHasSubtypePredicate(CardSubtype.HUMAN);
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.EXPLOIT, GrantScope.ALL_OWN_CREATURES, human));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"),
                GrantScope.ALL_OWN_CREATURES, human));

        addEffect(EffectSlot.ON_ALLY_CREATURE_EXPLOITS,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new ConditionalEffect(new EventValueAtLeast(3),
                                        CreateTokenEffect.ofTreasureToken(1)))));
    }
}
