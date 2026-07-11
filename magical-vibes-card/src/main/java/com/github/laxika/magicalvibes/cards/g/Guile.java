package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceControlledCounterWithExileAndPlayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "LRW", collectorNumber = "69")
public class Guile extends Card {

    public Guile() {
        // "This creature can't be blocked except by three or more creatures."
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
        // "If a spell or ability you control would counter a spell, instead exile that spell and you
        // may play that card without paying its mana cost."
        addEffect(EffectSlot.STATIC, new ReplaceControlledCounterWithExileAndPlayEffect());
        // "When Guile is put into a graveyard from anywhere, shuffle it into its owner's library."
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE, new ShuffleSelfFromGraveyardIntoLibraryEffect());
    }
}
