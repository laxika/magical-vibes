package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
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
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "89")
@CardRegistration(set = "NCC", collectorNumber = "97")
@CardRegistration(set = "OTC", collectorNumber = "138")
public class MariTheKillingQuill extends Card {

    public MariTheKillingQuill() {
        var outlaw = new PermanentHasAnySubtypePredicate(
                Set.of(CardSubtype.ASSASSIN, CardSubtype.MERCENARY, CardSubtype.ROGUE));

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new ExileTriggeringCreatureWithHitCounterEffect());
        addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES, outlaw));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                        new MayEffect(
                                new RemoveHitCounterFromExiledCardEffect(
                                        SequenceEffect.of(new DrawCardEffect(1),
                                                CreateTokenEffect.ofTreasureToken(2))),
                                "Remove a hit counter from a card that player owns in exile?")),
                        GrantScope.ALL_OWN_CREATURES, outlaw));
    }
}
