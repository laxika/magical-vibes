package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureWithHitCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveHitCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "89")
@CardRegistration(set = "NCC", collectorNumber = "97")
public class MariTheKillingQuill extends Card {

    public MariTheKillingQuill() {
        var outlaw = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.ASSASSIN, CardSubtype.MERCENARY, CardSubtype.ROGUE));

        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new ExileTriggeringCreatureWithHitCounterEffect());
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES, outlaw));
        addEffect(EffectSlot.STATIC,
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new MayEffect(
                                new RemoveHitCounterFromExiledCardEffect(
                                        SequenceEffect.of(new DrawCardEffect(1),
                                                CreateTokenEffect.ofTreasureToken(2))),
                                "Remove a hit counter from a card that player owns in exile?"),
                        GrantScope.OWN_CREATURES, outlaw));
    }
}
