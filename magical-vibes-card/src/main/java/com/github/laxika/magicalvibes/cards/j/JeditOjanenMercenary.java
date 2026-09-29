package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "34")
@CardRegistration(set = "DMC", collectorNumber = "56")
public class JeditOjanenMercenary extends Card {

    public JeditOjanenMercenary() {
        CreateTokenEffect catWarriorToken = new CreateTokenEffect(
                "Cat Warrior", 2, 2, CardColor.GREEN,
                List.of(CardSubtype.CAT, CardSubtype.WARRIOR),
                Set.of(Keyword.FORESTWALK), Set.of());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new MayPayManaEffect("{G}", catWarriorToken,
                                "Pay {G} to create a 2/2 green Cat Warrior creature token with forestwalk?")));
    }
}
