package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "9")
@CardRegistration(set = "MOC", collectorNumber = "95")
public class SaintTraftAndRemKarolus extends Card {

    public SaintTraftAndRemKarolus() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        SequenceEffect.of(
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(1),
                                        new CreateTokenEffect("Human", 1, 1, CardColor.RED,
                                                List.of(CardSubtype.HUMAN), Set.of(), Set.of())),
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(2),
                                        new CreateTokenEffect("Spirit", 1, 1, CardColor.BLUE,
                                                List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of())),
                                new ConditionalEffect(new NthAbilityResolutionThisTurn(3),
                                        new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                                                List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())))));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardKeywordPredicate(Keyword.CONVOKE),
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF))));
    }
}
