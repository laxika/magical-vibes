package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "79")
public class SkywiseTeachings extends Card {

    public SkywiseTeachings() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                noncreatureSpell,
                List.of(new MayPayManaEffect(
                        "{1}{U}",
                        new CreateTokenEffect("Djinn Monk", 2, 2, CardColor.BLUE,
                                List.of(CardSubtype.DJINN, CardSubtype.MONK),
                                Set.of(Keyword.FLYING), Set.of()),
                        "Pay {1}{U} to create a 2/2 blue Djinn Monk creature token with flying?"))
        ));
    }
}
