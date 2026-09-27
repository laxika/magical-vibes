package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "30")
public class Biotransference extends Card {

    public Biotransference() {
        addEffect(EffectSlot.STATIC,
                new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.STATIC,
                new GrantCardTypeToOwnCardsEffect(CardType.ARTIFACT,
                        new CardTypePredicate(CardType.CREATURE)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                List.of(
                        new LoseLifeEffect(1),
                        new CreateTokenEffect(1, "Necron Warrior", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.NECRON, CardSubtype.WARRIOR), Set.of(),
                                Set.of(CardType.ARTIFACT)))));
    }
}
