package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceHasDealtDamage;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "62")
public class RatonhnhakTon extends Card {

    public RatonhnhakTon() {
        ConditionalEffect hasNotDealtDamage = new ConditionalEffect(
                new NotCondition(new SourceHasDealtDamage()),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, hasNotDealtDamage);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new NotCondition(new SourceHasDealtDamage()),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));

        CreateTokenEffect assassin = new CreateTokenEffect(
                "Assassin", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.ASSASSIN), Set.of(Keyword.MENACE), Set.of());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenThenEffect(assassin,
                        new ReturnTargetEquipmentFromGraveyardAndAttachToCreatedTokenEffect()));
    }
}
