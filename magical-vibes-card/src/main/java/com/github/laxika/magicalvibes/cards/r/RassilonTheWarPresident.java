package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileExiledEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "150")
public class RassilonTheWarPresident extends Card {

    public RassilonTheWarPresident() {
        // At the beginning of your upkeep, you lose 2 life and exile the top card of your library.
        // You may play that card for as long as it remains exiled.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new LoseLifeEffect(2), new ExileTopCardMayPlayWhileExiledEffect()));

        // Each noncreature spell you cast from exile has conspire.
        addEffect(EffectSlot.STATIC, GrantSpellCastingAbilityToSpellsEffect.fromZone(
                Keyword.CONSPIRE,
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                Zone.EXILE));
    }
}
