package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1825")
@CardRegistration(set = "SLD", collectorNumber = "1853")
public class GoShintaiOfLifesOrigin extends Card {

    public GoShintaiOfLifesOrigin() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{U}{B}{R}{G}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.ENCHANTMENT))
                        .targetGraveyard(true)
                        .build()),
                "{W}{U}{B}{R}{G}, {T}: Return target enchantment card from your graveyard to the battlefield."
        ));

        CreateTokenEffect shrineToken = new CreateTokenEffect(
                "Shrine", 1, 1, null, List.of(CardSubtype.SHRINE), Set.of(), Set.of(CardType.ENCHANTMENT));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, shrineToken);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.SHRINE),
                                new CardNotPredicate(new CardIsTokenPredicate()))),
                        shrineToken));
    }
}
