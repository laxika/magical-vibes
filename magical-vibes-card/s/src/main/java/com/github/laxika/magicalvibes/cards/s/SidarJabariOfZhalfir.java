package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.condition.SourceCardOnBattlefield;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOC", collectorNumber = "5")
@CardRegistration(set = "MOC", collectorNumber = "97")
@CardRegistration(set = "MOC", collectorNumber = "138")
public class SidarJabariOfZhalfir extends Card {

    public SidarJabariOfZhalfir() {
        SequenceEffect attackEffect = SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        ConditionalEffect eminenceGate = new ConditionalEffect(
                new AnyOf(List.of(new SourceCardInCommandZone(), new SourceCardOnBattlefield())),
                attackEffect);
        ConditionalEffect knightAttacker = new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.KNIGHT)),
                eminenceGate);

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, knightAttacker);
        addEffect(EffectSlot.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK, knightAttacker);

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.KNIGHT))))
                        .targetGraveyard(true)
                        .build());
    }
}
