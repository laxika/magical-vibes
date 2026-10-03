package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "3")
@CardRegistration(set = "VOC", collectorNumber = "41")
public class DonalHeraldOfWings extends Card {

    public DonalHeraldOfWings() {
        var qualifyingCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardKeywordPredicate(Keyword.FLYING),
                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY))));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new OncePerTurnTriggerEffect(new MayEffect(
                        CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken(
                                qualifyingCreature, List.of(CardSubtype.SPIRIT), 1, 1),
                        "Copy that creature spell?")));
    }
}
