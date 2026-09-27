package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.WillOfThePlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "28")
@CardRegistration(set = "MOC", collectorNumber = "115")
public class PathOfTheSchemer extends Card {

    public PathOfTheSchemer() {
        // Each player mills two cards.
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.EACH_OPPONENT));
        // Then put a creature card from a graveyard onto the battlefield under your control.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .battlefieldEffectGrants(List.of(
                        new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.TARGET)))
                .build());
        addEffect(EffectSlot.SPELL, new WillOfThePlaneswalkersEffect());
    }
}
