package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.Flameshape;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileControllingSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "96")
public class GandalfGoblinsBaneFlameshape extends Card {

    public GandalfGoblinsBaneFlameshape() {
        setBackFaceCard(new Flameshape());
        addCastingOption(new AdventureCast("{1}{R}"));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(
                        new BoostSelfEffect(1, 1),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT))));
    }

    @Override
    public String getBackFaceClassName() {
        return "Flameshape";
    }
}
