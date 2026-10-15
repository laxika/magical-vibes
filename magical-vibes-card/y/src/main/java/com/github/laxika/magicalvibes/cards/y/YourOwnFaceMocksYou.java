package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.TargetGroupCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "366")
public class YourOwnFaceMocksYou extends Card {

    public YourOwnFaceMocksYou() {
        // Choose up to two target creatures your opponents control and create token copies of them.
        target(TargetFilters.creatureAnOpponentControls(), 0, 2)
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect());

        // If fewer than two copies were created, make up the difference with Scarecrows.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new Max(new Fixed(0), new Sum(new Fixed(2), new Scaled(new TargetGroupCount(0), -1))),
                "Scarecrow", 4, 4, null, List.of(CardSubtype.SCARECROW),
                Set.of(Keyword.VIGILANCE), Set.of(CardType.ARTIFACT)));
    }
}
