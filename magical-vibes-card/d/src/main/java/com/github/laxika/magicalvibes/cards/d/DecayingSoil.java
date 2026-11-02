package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "127")
public class DecayingSoil extends Card {

    public DecayingSoil() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ExileGraveyardCardsEffect(1, GraveyardExileScope.OWN));

        var threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.dies(
                                        new PermanentAllOfPredicate(List.of(
                                                Triggers.CREATURE,
                                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                                        ObjectRelation.YOU_OWN),
                                new MayPayManaEffect("{1}", new ReturnTriggeringCardToOwnerHandEffect(),
                                        "Pay {1} to return that card to your hand?")),
                        GrantScope.SELF)));
    }
}
