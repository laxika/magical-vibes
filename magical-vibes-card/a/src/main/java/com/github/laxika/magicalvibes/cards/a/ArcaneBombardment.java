package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "101")
@CardRegistration(set = "OTC", collectorNumber = "154")
public class ArcaneBombardment extends Card {

    public ArcaneBombardment() {
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        // Exile an instant or sorcery card at random from your graveyard. Then copy each card exiled with this
        // enchantment. You may cast any number of the copies without paying their mana costs.
        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery).nthThisTurn(1),
                SequenceEffect.of(
                        ExileCardAtRandomEffect.fromGraveyard(Subjects.you(), instantOrSorcery).withSource(),
                        CopyImprintedCardAndMayCastCopyEffect.of(Subjects.exiledWithSource()).castingAnyNumber()));
    }
}
