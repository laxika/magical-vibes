package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "46")
@CardRegistration(set = "FIC", collectorNumber = "146")
public class FandanielTelophoroiAscian extends Card {

    public FandanielTelophoroiAscian() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(instantOrSorcery, List.of(new SurveilEffect(1))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new EachOpponentMaySacrificeNontokenCreatureOrLoseLifeEffect(
                        new Scaled(new CardsInGraveyard(instantOrSorcery, CountScope.CONTROLLER), 2)));
    }
}
