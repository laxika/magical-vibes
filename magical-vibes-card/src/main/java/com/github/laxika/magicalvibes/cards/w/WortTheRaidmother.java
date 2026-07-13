package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantConspireToSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "223")
public class WortTheRaidmother extends Card {

    public WortTheRaidmother() {
        // When Wort enters, create two 1/1 red and green Goblin Warrior creature tokens.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Goblin Warrior", 1, 1,
                CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.GOBLIN, CardSubtype.WARRIOR)
        ));

        // Each red or green instant or sorcery spell you cast has conspire.
        addEffect(EffectSlot.STATIC, new GrantConspireToSpellsEffect(new CardAllOfPredicate(List.of(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                new CardAnyOfPredicate(List.of(
                        new CardColorPredicate(CardColor.RED),
                        new CardColorPredicate(CardColor.GREEN)))
        ))));
    }
}
