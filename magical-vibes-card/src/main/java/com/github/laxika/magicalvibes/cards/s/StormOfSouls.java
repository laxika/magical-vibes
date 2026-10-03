package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "156")
@CardRegistration(set = "VOC", collectorNumber = "9")
@CardRegistration(set = "VOC", collectorNumber = "47")
public class StormOfSouls extends Card {

    public StormOfSouls() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .returnAll(true)
                .grantSubtype(CardSubtype.SPIRIT)
                .battlefieldEffectGrants(List.of(
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, GrantDuration.INDEFINITE),
                        SetBasePowerToughnessEffect.indefinitely(1, 1)))
                .build());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
