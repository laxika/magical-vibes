package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "14")
public class GideonsPhalanx extends Card {

    public GideonsPhalanx() {
        // Create four 2/2 white Knight creature tokens with vigilance.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(4, "Knight", 2, 2, CardColor.WHITE,
                List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of(), false));

        // Spell mastery — If there are two or more instant and/or sorcery cards in your graveyard,
        // creatures you control gain indestructible until end of turn.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new GraveyardCardThreshold(2, new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))), new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_CREATURES)));
    }
}
