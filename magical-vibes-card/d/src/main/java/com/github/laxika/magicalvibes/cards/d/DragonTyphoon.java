package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YTDM", collectorNumber = "12")
public class DragonTyphoon extends Card {

    private static final List<String> SPELLBOOK = List.of(
            "Thunderbreak Regent",
            "Stormscale Scion",
            "Magmatic Hellkite",
            "Boltwing Marauder",
            "Neriv, Heart of the Storm",
            "Caldera Pyremaw",
            "Thundermane Dragon");

    public DragonTyphoon() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.DRAGON),
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))),
                        List.of(DraftCardFromSpellbookEffect.toBattlefield(SPELLBOOK, null))));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{R}",
                List.of(new CreateTokenEffect(
                        "Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())),
                "Channel — {2}{R}{R}, Discard this card: Create a 4/4 red Dragon creature token with flying."
        ));
    }
}
