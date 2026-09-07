package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TranceKujaFateDefied;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "232")
@CardRegistration(set = "FIN", collectorNumber = "399")
@CardRegistration(set = "FIN", collectorNumber = "497")
@CardRegistration(set = "FIN", collectorNumber = "544")
public class KujaGenomeSorcerer extends Card {

    public KujaGenomeSorcerer() {
        setBackFaceCard(new TranceKujaFateDefied());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, SequenceEffect.of(
                new CreateTokenEffect(
                        CardType.CREATURE, 1, "Wizard", 0, 1, CardColor.BLACK, null,
                        List.of(CardSubtype.WIZARD), Set.of(), Set.of(), false, true,
                        Map.of(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)))),
                        List.of(), false, false, false, 0, Set.of()),
                new ConditionalEffect(
                        new ControlsPermanentCount(4, new PermanentHasSubtypePredicate(CardSubtype.WIZARD)),
                        new TransformSelfEffect())));
    }

    @Override
    public String getBackFaceClassName() {
        return "TranceKujaFateDefied";
    }
}
