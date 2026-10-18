package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceHasSubtype;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetSourceBasePowerToughnessEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YWOE", collectorNumber = "24")
public class SecondLittlePig extends Card {

    public SecondLittlePig() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W/B}{W/B}",
                List.of(
                        PerpetuallyGrantCardCharacteristicsEffect.becomingCreatureTypes(Set.of(CardSubtype.BOAR, CardSubtype.SPIRIT)),
                        new PerpetuallySetSourceBasePowerToughnessEffect(4, 4),
                        new PerpetuallyGrantKeywordToSourceEffect(Keyword.FLYING)),
                "{2}{W/B}{W/B}: Second Little Pig perpetually becomes a Boar Spirit with base power and toughness "
                        + "4/4 and gains flying. Activate only if Second Little Pig isn't a Spirit."
        ).withActivationCondition(new NotCondition(new SourceHasSubtype(CardSubtype.SPIRIT)),
                "Activate only if Second Little Pig isn't a Spirit"));
    }
}
