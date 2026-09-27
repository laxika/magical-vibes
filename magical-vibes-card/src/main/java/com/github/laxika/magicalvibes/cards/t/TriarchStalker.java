package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentForTargetingRelayEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingRememberedPlayerPredicate;

@CardRegistration(set = "40K", collectorNumber = "67")
public class TriarchStalker extends Card {

    public TriarchStalker() {
        // At the beginning of combat on your turn, choose an opponent.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseOpponentForTargetingRelayEffect());

        // Creatures attacking the last chosen player have menace.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE, GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentIsAttackingRememberedPlayerPredicate()));
    }
}
