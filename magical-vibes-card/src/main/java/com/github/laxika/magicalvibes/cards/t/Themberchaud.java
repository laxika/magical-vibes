package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "728")
@CardRegistration(set = "SLX", collectorNumber = "28")
public class Themberchaud extends Card {

    public Themberchaud() {
        PermanentAllOfPredicate otherNonflyingCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        PermanentCount mountains = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER);

        // When Themberchaud enters, he deals damage to each other creature without flying and each player.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(mountains, true, false, otherNonflyingCreature));

        // Exert: "You may exert this creature as it attacks. When you do, it gains flying until end of turn."
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Themberchaud as it attacks? (It gains flying until end of turn.)"
        ));
    }
}
