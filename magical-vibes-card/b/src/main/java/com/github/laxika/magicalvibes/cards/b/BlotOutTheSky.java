package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "167")
public class BlotOutTheSky extends Card {

    public BlotOutTheSky() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new XValue(),
                "Inkling",
                2,
                1,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.INKLING),
                Set.of(Keyword.FLYING),
                Set.of(),
                false,
                true,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()));

        var noncreatureNonland = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(6),
                new DestroyAllPermanentsEffect(noncreatureNonland)));
    }
}
