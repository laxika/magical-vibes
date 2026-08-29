package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastLesserSpellWithSharedTypeOrCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "218")
public class BaralAndKariZev extends Card {

    private static final CreateTokenEffect RAGAVAN_TOKEN = new CreateTokenEffect(
            CardType.CREATURE,
            1,
            "First Mate Ragavan",
            2,
            1,
            CardColor.RED,
            null,
            List.of(CardSubtype.MONKEY, CardSubtype.PIRATE),
            Set.of(),
            Set.of(),
            false,
            false,
            Map.of(),
            List.of(),
            false,
            false,
            true,
            0,
            Set.of(Keyword.HASTE));

    public BaralAndKariZev() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                SpellCastTriggerEffect.nth(
                        1,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        List.of(new MayCastLesserSpellWithSharedTypeOrCreateTokenEffect(RAGAVAN_TOKEN))));
    }
}
