package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "16")
@CardRegistration(set = "SOC", collectorNumber = "66")
public class VanguardOfTheRestless extends Card {

    public VanguardOfTheRestless() {
        var commanderCasts = new CommanderCastsFromCommandZoneThisGame();
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                commanderCasts, commanderCasts, GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SPIRIT))));

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SPIRIT),
                        new MayPayManaEffect("{2}{W}",
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                        .filter(new CardIsSelfPredicate())
                                        .returnAll(true)
                                        .build(),
                                "Pay {2}{W} to return Vanguard of the Restless from your graveyard to the battlefield?")));
    }
}
