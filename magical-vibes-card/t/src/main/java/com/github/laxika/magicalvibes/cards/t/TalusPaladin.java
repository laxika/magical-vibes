package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "21")
public class TalusPaladin extends Card {

    public TalusPaladin() {
        PermanentHasSubtypePredicate ally = new PermanentHasSubtypePredicate(CardSubtype.ALLY);
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardAnyOfPredicate(List.of(
                        new CardIsSelfPredicate(), new CardSubtypePredicate(CardSubtype.ALLY))),
                        SequenceEffect.of(
                                new MayEffect(SequenceEffect.of(
                                        new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.OWN_CREATURES, ally),
                                        new ConditionalEffect(new SourceMatchesPermanentPredicate(
                                                new PermanentAllOfPredicate(List.of(ally,
                                                        new PermanentControlledBySourceControllerPredicate()))),
                                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF))
                                ), "Have Ally creatures you control gain lifelink until end of turn?"),
                                new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                                        "Put a +1/+1 counter on Talus Paladin?"))));
    }
}
