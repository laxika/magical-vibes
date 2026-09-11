package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "205")
public class AvengersUnderSiege extends Card {

    public AvengersUnderSiege() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new CreateTokenEffect(2, "Villain", 2, 1, CardColor.BLACK,
                        List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of()));

        var nonVillainCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VILLAIN))));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MassDamageEffect(2, false, false, nonVillainCreature));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT));

        addEffect(EffectSlot.SAGA_CHAPTER_III,
                CreateTokenEffect.ofTreasureToken(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.VILLAIN), CountScope.CONTROLLER)));
    }
}
