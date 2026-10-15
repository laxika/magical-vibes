package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "25")
@CardRegistration(set = "TMT", collectorNumber = "226")
public class TriceratonCommander extends Card {

    public TriceratonCommander() {
        PermanentPredicate otherDinosaur = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        addEffect(EffectSlot.ON_ATTACK, new BoostAllOwnCreaturesEffect(1, 1, otherDinosaur));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(
                Keyword.FLYING, GrantScope.OWN_CREATURES, otherDinosaur));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new XValue(), "Dinosaur Soldier", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.DINOSAUR, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
