package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MUL", collectorNumber = "60")
@CardRegistration(set = "MUL", collectorNumber = "125")
@CardRegistration(set = "MUL", collectorNumber = "190")
public class TaigamOjutaiMaster extends Card {

    public TaigamOjutaiMaster() {
        CardAnyOfPredicate instantSorceryOrDragon = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY),
                new CardSubtypePredicate(CardSubtype.DRAGON)));
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        // Instant, sorcery, and Dragon spells you control can't be countered.
        addEffect(EffectSlot.STATIC, new ControllerSpellsCantBeCounteredEffect(instantSorceryOrDragon));

        // Whenever you cast an instant or sorcery spell from your hand, if Taigam attacked this
        // turn, that spell gains rebound.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                instantOrSorcery,
                List.of(new GrantKeywordsToCastSpellEffect(Set.of(Keyword.REBOUND))),
                null,
                null,
                new StackEntryCastFromZonePredicate(Zone.HAND),
                false,
                false,
                new SourceAttackedThisTurn(),
                0));
    }
}
