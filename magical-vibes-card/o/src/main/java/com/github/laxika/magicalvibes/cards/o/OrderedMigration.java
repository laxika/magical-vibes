package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "258")
public class OrderedMigration extends Card {

    public OrderedMigration() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new BasicLandTypesAmongControlledLands(),
                "Bird", 1, 1, CardColor.BLUE,
                List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
    }
}
