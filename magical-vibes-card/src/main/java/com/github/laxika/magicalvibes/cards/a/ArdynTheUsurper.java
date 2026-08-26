package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "89")
public class ArdynTheUsurper extends Card {

    public ArdynTheUsurper() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Set.of(Keyword.MENACE, Keyword.LIFELINK, Keyword.HASTE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DEMON)));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ExileTargetCardFromGraveyardAndCreateTokenCopyEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        false,
                        List.of(CardSubtype.DEMON),
                        false,
                        false,
                        CardColor.BLACK,
                        5,
                        5,
                        Set.of(),
                        false,
                        false,
                        null,
                        true));
    }
}
