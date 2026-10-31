package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "125")
public class FeloniousRage extends Card {

    public FeloniousRage() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new BoostTargetCreatureEffect(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addSpellEffect(new ResolveEffectOnTargetDeathThisTurnEffect(
                        new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                                Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE))));
    }
}
