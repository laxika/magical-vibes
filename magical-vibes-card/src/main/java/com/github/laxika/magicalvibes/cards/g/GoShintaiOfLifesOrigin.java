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
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1825")
@CardRegistration(set = "SLD", collectorNumber = "1853")
@CardRegistration(set = "HA6", collectorNumber = "6")
public class GoShintaiOfLifesOrigin extends Card {

    public GoShintaiOfLifesOrigin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, shrineToken());

        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        shrineToken()));

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
    }

    private static CreateTokenEffect shrineToken() {
        return new CreateTokenEffect("Shrine", 1, 1, null,
                List.of(CardSubtype.SHRINE), Set.of(), Set.of(CardType.ENCHANTMENT));
    }
}
