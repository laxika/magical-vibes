package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "96")
@CardRegistration(set = "FIC", collectorNumber = "189")
public class UriangerAugurelt extends Card {

    public UriangerAugurelt() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new GainLifeEffect(2)),
                        new StackEntryCastFromZonePredicate(Zone.EXILE)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new LandPlayFromExileTriggerEffect(List.of(new GainLifeEffect(2))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MayEffect(new ExileTopCardsToSourceEffect(1), "Exile it face down?")),
                "{T}: Look at the top card of your library. You may exile it face down."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AllowPlayCardsExiledWithSourceUntilEndOfTurnEffect(2)),
                "{T}: Until end of turn, you may play cards exiled with Urianger Augurelt. "
                        + "Spells you cast this way cost {2} less to cast."
        ));
    }
}
