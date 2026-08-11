package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "132")
public class HighlandBerserker extends Card {

    public HighlandBerserker() {
        // Whenever this creature or another Ally you control enters, you may have Ally creatures
        // you control gain first strike until end of turn.
        PermanentHasSubtypePredicate ally = new PermanentHasSubtypePredicate(CardSubtype.ALLY);
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ALLY),
                        new MayEffect(SequenceEffect.of(
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES, ally),
                                new ConditionalEffect(new SourceHasSubtype(CardSubtype.ALLY),
                                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF))
                        ), "Have Ally creatures you control gain first strike until end of turn?")));
    }
}
