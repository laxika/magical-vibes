package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LandPlayFromExileTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "78")
@CardRegistration(set = "SPM", collectorNumber = "202")
@CardRegistration(set = "SPM", collectorNumber = "209")
public class GwenStacy extends Card {

    public GwenStacy() {
        setBackFaceCard(new GhostSpider());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardOfOwnLibraryEffect(true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{R}{W}",
                List.of(new TransformSelfEffect()),
                "{2}{U}{R}{W}: Transform Gwen Stacy. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "GhostSpider";
    }
}

class GhostSpider extends Card {

    GhostSpider() {
        addEffect(EffectSlot.STATIC, new AllowCastFromCardsExiledWithSourceEffect(false));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new StackEntryCastFromZonePredicate(Zone.EXILE)));
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND,
                new LandPlayFromExileTriggerEffect(
                        List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.ANY),
                        new ExileTopCardMayPlayThisTurnEffect(false)
                ),
                "Remove two counters from Ghost-Spider: Exile the top card of your library. You may play that card this turn."
        ));
    }
}
