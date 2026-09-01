package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "101")
public class TheFinalDays extends Card {

    public TheFinalDays() {
        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);

        CreateTokenEffect normalTokens = new CreateTokenEffect(2, "Horror", 2, 2,
                CardColor.BLACK, List.of(CardSubtype.HORROR), Set.of(), Set.of(), true);
        CreateTokenEffect flashbackTokens = new CreateTokenEffect(
                CardType.CREATURE, creatureCards, "Horror", 2, 2, CardColor.BLACK, null,
                List.of(CardSubtype.HORROR), Set.of(), Set.of(), false, true,
                Map.of(), List.of(), false, false, false, 0, Set.of());

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.GRAVEYARD), normalTokens, flashbackTokens));
        addCastingOption(new FlashbackCast("{4}{B}{B}"));
    }
}
