package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "99")
public class LordSkittersButcher extends Card {

    public LordSkittersButcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 1/1 black Rat creature token with \"This token can't block.\"",
                        new CreateTokenEffect(
                                1, "Rat", 1, 1, CardColor.BLACK, List.of(CardSubtype.RAT),
                                Set.of(), Set.of(), Map.of(EffectSlot.STATIC, new CantBlockEffect()))),
                new ChooseOneEffect.ChooseOneOption(
                        "You may sacrifice another creature. If you do, scry 2, then draw a card.",
                        new MayEffect(
                                new SacrificePermanentThenEffect(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                                        )),
                                        SequenceEffect.of(new ScryEffect(2), new DrawCardEffect()),
                                        "another creature", false, false),
                                "Sacrifice another creature?")),
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control gain menace until end of turn.",
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES))
        )));
    }
}
