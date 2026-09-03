package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "177")
public class CormelaGlamourThief extends Card {

    public CormelaGlamourThief() {
        ManaRestriction instantOrSorcery = new ManaRestriction.SpellTypes(
                Set.of(CardType.INSTANT, CardType.SORCERY));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.BLUE, 1, instantOrSorcery),
                        new AwardRestrictedManaEffect(ManaColor.BLACK, 1, instantOrSorcery),
                        new AwardRestrictedManaEffect(ManaColor.RED, 1, instantOrSorcery)
                ),
                "{T}: Add {U}{B}{R}. Spend this mana only to cast instant and/or sorcery spells."
        ));

        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )))
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
