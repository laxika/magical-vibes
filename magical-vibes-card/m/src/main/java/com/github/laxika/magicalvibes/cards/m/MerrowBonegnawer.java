package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "37")
public class MerrowBonegnawer extends Card {

    public MerrowBonegnawer() {
        // {T}: Target player exiles a card from their graveyard.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(new TargetPlayerExilesCardFromGraveyardEffect(0)),
                "{T}: Target player exiles a card from their graveyard."
        ));

        // Whenever you cast a black spell, you may untap this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        new CardColorPredicate(CardColor.BLACK),
                        List.of(new UntapPermanentsEffect(TapUntapScope.SELF))),
                "Untap Merrow Bonegnawer?"));
    }
}
