package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "215")
public class RivazOfTheClaw extends Card {

    public RivazOfTheClaw() {
        CardAllOfPredicate dragonCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.DRAGON)));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_CREATURE_SPELL,
                                CardSubtype.DRAGON),
                        new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_CREATURE_SPELL,
                                CardSubtype.DRAGON)
                ),
                "{T}: Add two mana in any combination of colors. Spend this mana only to cast Dragon creature spells."
        ));

        addEffect(EffectSlot.STATIC, new PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
                dragonCreature, null));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                dragonCreature,
                List.of(new GrantTriggeredAbilityToCastSpellEffect(
                        EffectSlot.ON_DEATH, new ExileSourceCardFromGraveyardEffect())),
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD)));
    }
}
