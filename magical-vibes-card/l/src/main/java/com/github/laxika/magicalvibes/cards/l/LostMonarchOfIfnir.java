package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerDealtCombatDamageBySubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DRC", collectorNumber = "11")
@CardRegistration(set = "DRC", collectorNumber = "27")
public class LostMonarchOfIfnir extends Card {

    public LostMonarchOfIfnir() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new LoseLifeEffect(3, LoseLifeRecipient.DEFENDING_PLAYER));

        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_BLOCKED,
                new LoseLifeEffect(3, LoseLifeRecipient.DEFENDING_PLAYER),
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)));

        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new ConditionalEffect(
                new AnyPlayerDealtCombatDamageBySubtypeThisTurn(CardSubtype.ZOMBIE),
                SequenceEffect.of(
                        new MillEffect(3, MillRecipient.CONTROLLER),
                        new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .build(),
                                "Return a creature card from your graveyard to your hand?"))));
    }
}
