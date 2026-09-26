package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YDMU", collectorNumber = "15")
public class SpellchainScatter extends Card {

    public SpellchainScatter() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{U}"));

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        instantOrSorcery,
                        List.of(new ConjureDuplicateOfTriggeringSpellIntoHandEffect(false)),
                        true,
                        false)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotKicked(),
                new RegisterDelayedControllerSpellCastTriggerEffect(
                        instantOrSorcery,
                        List.of(new ConjureDuplicateOfTriggeringSpellIntoHandEffect(true)),
                        true,
                        false)));
    }
}
