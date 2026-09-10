package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AncestorsEmbrace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;

@CardRegistration(set = "VOW", collectorNumber = "22")
public class KindlyAncestor extends Card {

    public KindlyAncestor() {
        setBackFaceCard(new AncestorsEmbrace());
        addCastingOption(new DisturbCast("{1}{W}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "AncestorsEmbrace";
    }
}
