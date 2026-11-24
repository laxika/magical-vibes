package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceTokenSubtypeCreationEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "10")
@CardRegistration(set = "2X2", collectorNumber = "8")
@CardRegistration(set = "RVR", collectorNumber = "16")
@CardRegistration(set = "RVR", collectorNumber = "306")
@CardRegistration(set = "MSC", collectorNumber = "131")
@CardRegistration(set = "MSC", collectorNumber = "301")
@CardRegistration(set = "TDC", collectorNumber = "113")
public class DivineVisitation extends Card {

    public DivineVisitation() {
        addStaticEffect(ReplaceTokenSubtypeCreationEffect.creatureTokens(new CreateTokenEffect(
                "Angel", 4, 4, CardColor.WHITE, List.of(CardSubtype.ANGEL),
                Set.of(Keyword.FLYING, Keyword.VIGILANCE), Set.of())));
    }
}
