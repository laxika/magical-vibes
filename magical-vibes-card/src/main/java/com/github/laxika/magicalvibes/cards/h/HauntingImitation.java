package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardAndCreatesTokenCopyEffect;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "VOC", collectorNumber = "13")
@CardRegistration(set = "VOC", collectorNumber = "51")
public class HauntingImitation extends Card {

    public HauntingImitation() {
        addEffect(EffectSlot.SPELL, new EachPlayerRevealsTopCardAndCreatesTokenCopyEffect(
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(CardSubtype.SPIRIT), Set.of(), 1, 1, Map.of(), null,
                        Set.of(Keyword.FLYING))));
    }
}
