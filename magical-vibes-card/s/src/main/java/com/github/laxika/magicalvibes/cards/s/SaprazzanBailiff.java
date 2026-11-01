package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "97")
public class SaprazzanBailiff extends Card {

    public SaprazzanBailiff() {
        CardPredicate artifactOrEnchantment = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.ENCHANTMENT)
        ));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.ALL_PLAYERS, artifactOrEnchantment));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(artifactOrEnchantment)
                        .returnAll(true)
                        .build());
    }
}
