package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsExiledWithSource;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DTK", collectorNumber = "61")
public class LivingLore extends Card {

    public LivingLore() {
        var instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        var exiledManaValue = new GreatestManaValueAmongCardsExiledWithSource();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileCardFromGraveyardOnEnterEffect(instantOrSorcery));
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(exiledManaValue, exiledManaValue));
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                new MayEffect(
                        new SacrificeSelfThenEffect(new MayCastCardExiledWithSourceEffect()),
                        "You may sacrifice it. If you do, you may cast the exiled card without paying its mana cost."));
    }
}
