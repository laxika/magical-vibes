package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "9")
@CardRegistration(set = "DSC", collectorNumber = "41")
public class RedressFate extends Card {

    public RedressFate() {
        // Return all artifact and enchantment cards from your graveyard to the battlefield.
        // Miracle {3}{W}
        addCastingOption(new MiracleCast("{3}{W}"));
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .chooseAuraAttachment(true)
                .filter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardTypePredicate(CardType.ENCHANTMENT)
                )))
                .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                .returnAll(true)
                .build());
    }
}
