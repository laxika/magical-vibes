package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "250")
public class SigardaHeronsGrace extends Card {

    public SigardaHeronsGrace() {
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.HEXPROOF,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileCardFromGraveyardCost((CardType) null),
                        new CreateTokenEffect("Human Soldier", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of())
                ),
                "{2}, Exile a card from your graveyard: Create a 1/1 white Human Soldier creature token."
        ));
    }
}
