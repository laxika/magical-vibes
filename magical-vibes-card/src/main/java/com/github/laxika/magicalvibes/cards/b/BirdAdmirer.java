package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MID", collectorNumber = "169")
public class BirdAdmirer extends Card {

    public BirdAdmirer() {
        setBackFaceCard(new WingShredder());
    }

    @Override
    public String getBackFaceClassName() {
        return "WingShredder";
    }
}
