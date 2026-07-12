package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "78")
public class SmolderInitiate extends Card {

    public SmolderInitiate() {
        // Whenever a player casts a black spell, you may pay {1}. If you do, target player loses 1 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardColorPredicate(CardColor.BLACK),
                        List.of(new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER)),
                        "{1}"),
                "Pay {1} to make target player lose 1 life?"
        ));
    }
}
